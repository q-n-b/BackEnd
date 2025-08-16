package qnb.question.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Value;
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

    // 동기 타임박스(기본 3초)
    @org.springframework.beans.factory.annotation.Value("${ml.sync-timeout-sec:3}")
    private int syncTimeoutSec;

    // 프론트 폴링 힌트(기본 2초)
    @org.springframework.beans.factory.annotation.Value("${ml.next-check-after-sec:2}")
    private int nextCheckAfterSec;

    /**
     * 한 도서당 GPT 질문 1개 정책:
     * - READY 존재 → 409
     * - GENERATING 존재 → 200(공유)
     * - FAILED 존재 → 200(재시도 UI 안내; 자동 생성 안함)
     * - 없으면 → 드래프트 생성(GENERATING), 동기 타임박스→백그라운드 이어서
     */
    @Transactional
    public Result generate(Integer bookId) {
        Long gptUserId = gptProps.getUserId();
        if (gptUserId == null) throw new InternalErrorException("GPT 시스템 사용자 미초기화");

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("질문을 등록할 책을 찾을 수 없습니다."));

        // 1) 비관적 락으로 기존 행 선점 (중복 생성을 최대한 차단)
        Optional<Question> lockedOpt =
                questionRepository.findForUpdateByBookIdAndUserId(bookId, gptUserId);

        if (lockedOpt.isPresent()) {
            Question existing = lockedOpt.get();
            switch (existing.getStatus()) {
                case READY:
                    return Result.conflict409();
                case GENERATING:
                    return Result.ok200(existing.getQuestionId(),
                            Question.QuestionStatus.GENERATING, nextCheckAfterSec);
                case FAILED:
                    return Result.ok200(existing.getQuestionId(),
                            Question.QuestionStatus.FAILED, null);
            }
        }

        // 2) 존재하지 않으면 신규 드래프트 생성(GENERATING)
        Question draft = new Question();
        draft.setBook(book);
        draft.setUser(userRepository.getReferenceById(gptUserId)); // GPT 계정 연결
        draft.setStatus(Question.QuestionStatus.GENERATING);

        try {
            draft = questionRepository.save(draft);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 극단적 레이스: 동시에 "없다"고 판단 후 양쪽이 INSERT 시도 → 유니크 충돌
            Question existing = questionRepository
                    .findByBook_BookIdAndUser_UserId(bookId, gptUserId)
                    .orElseThrow(() -> e);

            if (existing.getStatus() == Question.QuestionStatus.READY) {
                return Result.conflict409();
            }
            if (existing.getStatus() == Question.QuestionStatus.GENERATING) {
                return Result.ok200(existing.getQuestionId(),
                        Question.QuestionStatus.GENERATING, nextCheckAfterSec);
            }
            return Result.ok200(existing.getQuestionId(),
                    Question.QuestionStatus.FAILED, null);
        }

        Integer qid = draft.getQuestionId();

        // 3) 동기 타임박스 시도 (2~3초 컷)
        try {
            String content = trySyncGenerate(book);
            if (content != null) {
                draft.setQuestionContent(content);
                draft.setStatus(Question.QuestionStatus.READY);
                return Result.created201(qid, Question.QuestionStatus.READY, null);
            }
        } catch (MlClientException e) {
            // 동기 구간에서도 4xx 등 비복구 오류면 바로 FAILED 저장
            draft.markFailed(truncate(e.getMessage(), 480));
            return Result.created201(qid, Question.QuestionStatus.FAILED, null);
        } catch (Exception e) {
            // TIMEOUT 등의 경우는 백그라운드로 이어서 처리
            log.info("GPT 동기 생성 실패/타임아웃: {}", e.getMessage());
        }

        // 4) 타임아웃/실패 → 백그라운드에서 이어서 처리
        continueInBackground(qid);
        return Result.created201(qid, Question.QuestionStatus.GENERATING, nextCheckAfterSec);
    }

    /** ML 호출 예외: 상태코드/바디를 메시지로 담아 lastError에 저장하기 위함 */
    public static class MlClientException extends RuntimeException {
        private final int status;
        public MlClientException(int status, String body) {
            super("ML_" + status + (body != null && !body.isEmpty() ? (": " + body) : ""));
            this.status = status;
        }
        public int getStatus() { return status; }
    }

    /** ML 서버 동기 호출 (짧게 시도, 실패 시 예외) */
    private String trySyncGenerate(Book book) {
        GenerateReq req = new GenerateReq(
                book.getBookId().longValue(),
                safe(book.getTitle())
        );

        return mlWebClient.post()
                .uri("/generate-question") // ML 엔드포인트 경로
                .bodyValue(req)
                .exchangeToMono(resp -> {
                    HttpStatusCode sc = resp.statusCode();
                    if (sc.is2xxSuccessful()) {
                        return resp.bodyToMono(String.class);
                    } else {
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new MlClientException(sc.value(), body)));
                    }
                })
                .timeout(Duration.ofSeconds(syncTimeoutSec))
                .block();
    }

    @Async("gptWorker")
    @Transactional
    public void continueInBackground(Integer questionId) {
        try {
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q == null || q.getStatus() == Question.QuestionStatus.READY) return;

            String content = trySyncGenerate(q.getBook());
            q.setQuestionContent(content);
            q.setStatus(Question.QuestionStatus.READY);

        } catch (MlClientException e) {
            // 4xx 등 → 비복구: 즉시 FAILED + 사유 저장
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q != null) q.markFailed(truncate(e.getMessage(), 480));

        } catch (Exception e) {
            // TIMEOUT/네트워크/5xx 등 → 실패로 마킹(사유 저장)
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q != null) q.markFailed(truncate(e.getMessage(), 480));
        }
    }

    private String safe(String s) { return s == null ? "" : s.replace("\"","'"); }
    private String truncate(String s, int max) { return s == null ? null : (s.length() <= max ? s : s.substring(0, max)); }

    // === 응답 뷰모델 ===
    @Value
    public static class Result {
        int httpStatus;                 // 200 / 201 / 409
        Integer questionId;
        Question.QuestionStatus status;
        Integer nextCheckAfterSec;
        String errorCode;
        String errorMessage;

        public static Result ok200(Integer qid, Question.QuestionStatus st, Integer waitSec) {
            return new Result(200, qid, st, waitSec, null, null);
        }
        public static Result created201(Integer qid, Question.QuestionStatus st, Integer waitSec) {
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
