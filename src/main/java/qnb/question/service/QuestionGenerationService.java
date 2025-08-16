package qnb.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.common.config.GptUserProperties;
import qnb.common.exception.BookNotFoundException;
import qnb.common.exception.InternalErrorException;
import qnb.question.entity.Question;
import qnb.question.model.QuestionStatus;
import qnb.question.repository.QuestionRepository;
import qnb.user.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerationService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final WebClient mlWebClient;      // MlClientConfig 주입
    private final UserRepository userRepository;
    private final GptUserProperties gptProps; // 시스템 GPT 계정

    // 동기 타임박스(기본 3초) — 필요하면 yml에 ml.sync-timeout-sec 로 오버라이드
    @org.springframework.beans.factory.annotation.Value("${ml.sync-timeout-sec:3}")
    private int syncTimeoutSec;

    // 프론트 폴링 힌트(기본 2초)
    @org.springframework.beans.factory.annotation.Value("${ml.next-check-after-sec:2}")
    private int nextCheckAfterSec;

    @Transactional
    public Result generate(Integer bookId) {
        Long gptUserId = gptProps.getUserId();
        if (gptUserId == null) throw new InternalErrorException("GPT 시스템 사용자 미초기화");

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("질문을 등록할 책을 찾을 수 없습니다."));

        // 1) READY 존재 → 409
        Optional<Question> existOpt = questionRepository.findByBook_BookIdAndUser_UserId(bookId, gptUserId);
        if (existOpt.isPresent() && existOpt.get().getStatus() == QuestionStatus.READY) {
            return Result.conflict409();
        }

        // 2) GENERATING 진행 중 → 200
        if (existOpt.isPresent() && existOpt.get().getStatus() == QuestionStatus.GENERATING) {
            return Result.ok200(existOpt.get().getQuestionId(), QuestionStatus.GENERATING, nextCheckAfterSec);
        }

        // 3) 둘 다 없음 → 드래프트 생성(GENERATING)
        Question draft = new Question();
        draft.setBook(book);
        draft.setUser(userRepository.getReferenceById(gptUserId)); // GPT 계정 연결
        draft.setStatus(QuestionStatus.GENERATING);

        try {
            draft = questionRepository.save(draft);
        } catch (DataIntegrityViolationException e) {
            // 동시 생성 레이스: 유니크(book_id, user_id) 충돌 → 재조회로 회복
            Question existing = questionRepository
                    .findByBook_BookIdAndUser_UserId(bookId, gptUserId)
                    .orElseThrow(() -> e);

            if (existing.getStatus() == QuestionStatus.READY) {
                return Result.conflict409(); // 이미 다른 트랜잭션이 READY 완성
            }
            // 아직 생성중이라면 진행중 상태 공유
            return Result.ok200(existing.getQuestionId(), QuestionStatus.GENERATING, nextCheckAfterSec);
        }

        Integer qid = draft.getQuestionId();

        // 3-a) 동기 타임박스 시도
        String content = trySyncGenerate(book);
        if (content != null) {
            draft.setQuestionContent(content);
            draft.setStatus(QuestionStatus.READY);
            return Result.created201(qid, QuestionStatus.READY, null);
        }

        // 3-b) 타임아웃/실패 → 백그라운드 이어서
        continueInBackground(qid);
        return Result.created201(qid, QuestionStatus.GENERATING, nextCheckAfterSec);
    }

    private String trySyncGenerate(Book book) {
        GenerateReq req = new GenerateReq(
                book.getBookId().longValue(),
                safe(book.getTitle())
        );
        try {
            return mlWebClient.post()
                    .uri("/generate-question") // ML 서버 엔드포인트 경로
                    .bodyValue(req)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.createException().
                            flatMap(Mono::error))
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(syncTimeoutSec)) // 2~3초 컷
                    .block();
        } catch (Exception e) {
            log.info("GPT 동기 생성 실패/타임아웃: {}", e.getMessage());
            return null;
        }
    }

    @Async("gptWorker")
    @Transactional
    public void continueInBackground(Integer questionId) {
        try {
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q == null || q.getStatus() == QuestionStatus.READY) return;

            String content = trySyncGenerate(q.getBook());
            if (content != null) {
                q.setQuestionContent(content);
                q.setStatus(QuestionStatus.READY);
            } else {
                q.setStatus(QuestionStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("백그라운드 생성 실패 questionId={}: {}", questionId, e.getMessage());
        }
    }

    private String safe(String s) { return s == null ? "" : s.replace("\"","'"); }

    // === 응답 뷰모델 ===
    @Value
    public static class Result {
        int httpStatus;                 // 200 / 201 / 409
        Integer questionId;
        QuestionStatus status;
        Integer nextCheckAfterSec;
        String errorCode;
        String errorMessage;

        public static Result ok200(Integer qid, QuestionStatus st, Integer waitSec) {
            return new Result(200, qid, st, waitSec, null, null);
        }
        public static Result created201(Integer qid, QuestionStatus st, Integer waitSec) {
            return new Result(201, qid, st, waitSec, null, null);
        }
        public static Result conflict409() {
            return new Result(409, null, null, null,
                    "QuestionAlreadyExistsException", "해당 도서에 대한 질문이 이미 존재합니다.");
        }
    }

    // === ML 요청 DTO ===
    @Value
    public static class GenerateReq {
        Long bookId;
        String title;
    }
}
