package qnb.question.service;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
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
import qnb.question.client.dto.MlGenerateReq;
import qnb.question.client.dto.MlGenerateResp;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.user.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

import static qnb.question.entity.Question.QuestionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerationService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final WebClient mlWebClient;      // MlClientConfig에서 주입
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
     * - FAILED 존재 → 200 (재시도 UI 노출; 자동 생성 안함)
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
                //질문 존재하면 409
                case READY:
                    return Result.conflict409();
                    //질문 생성 중
                case GENERATING:
                    return Result.ok200(existing.getQuestionId(), GENERATING, nextCheckAfterSec);
                    //FAILED일 경우 재시도 UI 노출
                case FAILED:
                    return Result.ok200(existing.getQuestionId(), FAILED, null);
            }
        }

        // 2) 존재하지 않으면 신규 드래프트 생성(GENERATING)
        Question draft = new Question();
        draft.setBook(book);
        draft.setUser(userRepository.getReferenceById(gptUserId));
        draft.setStatus(GENERATING);

        try {
            draft = questionRepository.save(draft);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 극단적 레이스 → 유니크 충돌
            Question existing = questionRepository
                    .findByBook_BookIdAndUser_UserId(bookId, gptUserId)
                    .orElseThrow(() -> e);

            if (existing.getStatus() == READY) return Result.conflict409();
            if (existing.getStatus() == GENERATING) return Result.ok200(existing.getQuestionId(), GENERATING, nextCheckAfterSec);
            return Result.ok200(existing.getQuestionId(), FAILED, null);
        }

        Integer qid = draft.getQuestionId();

        // 3) 동기 타임박스 시도
        try {
            MlGenerateResp resp = trySyncGenerate(book);
            if (resp != null) {
                if (resp.blocked()) {
                    // 정책 차단 → FAILED
                    // ML에서 reason 받아옴 (어떤 문자열이든 상관 X)
                    // 실패 -> FAILED
                    draft.markFailed(truncate("blocked:" + safe(resp.reason()), 480));
                    return Result.created201(qid, FAILED, null);
                } else {
                    // 정상 생성 → READY
                    draft.setQuestionContent(resp.question());
                    draft.setStatus(READY);
                    return Result.created201(qid, READY, null);
                }
            }

        } catch (MlClientException e) {
            draft.markFailed(truncate(e.getMessage(), 480));
            return Result.created201(qid, FAILED, null);
        } catch (Exception e) {
            log.info("GPT 동기 생성 실패/타임아웃: {}", e.getMessage());
        }

        // 4) 타임아웃/실패 → 백그라운드에서 이어서 처리
        continueInBackground(qid);
        return Result.created201(qid, GENERATING, nextCheckAfterSec);
    }

    /** ML 호출 예외: 상태코드/바디를 메시지로 담음 */
    public static class MlClientException extends RuntimeException {
        private final int status;
        public MlClientException(int status, String body) {
            super("ML_" + status + (body != null && !body.isEmpty() ? (": " + body) : ""));
            this.status = status;
        }
        public int getStatus() { return status; }
    }

    /** ML 서버 동기 호출 (DTO 기반) */
    private MlGenerateResp trySyncGenerate(Book book) {
        MlGenerateReq req = new MlGenerateReq(
                book.getBookId().longValue(),
                safe(book.getTitle())
        );

        return mlWebClient.post()
                .uri("/generate-question")
                .bodyValue(req)
                .exchangeToMono(resp -> {
                    HttpStatusCode sc = resp.statusCode();
                    if (sc.is2xxSuccessful()) {
                        return resp.bodyToMono(MlGenerateResp.class);
                    } else {
                        return resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new MlClientException(sc.value(), body)));
                    }
                })
                .timeout(Duration.ofSeconds(syncTimeoutSec))
                .block();
    }

    //타임아웃시 백그라운드 이어서 처리
    @Async("gptWorker")
    @Transactional
    public void continueInBackground(Integer questionId) {
        try {
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q == null || q.getStatus() == READY) return;

            MlGenerateResp resp = trySyncGenerate(q.getBook());
            if (resp == null) {
                q.markFailed(truncate("ml_call_failed: timeout_or_transport_error", 480));
                return;
            }
            if (resp.blocked()) {
                q.markFailed(truncate("blocked:" + safe(resp.reason()), 480));
                return;
            }
            q.setQuestionContent(resp.question());
            q.setStatus(READY);

        } catch (MlClientException e) {
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q != null) q.markFailed(truncate(e.getMessage(), 480));

        } catch (Exception e) {
            Question q = questionRepository.findById(questionId).orElse(null);
            if (q != null) q.markFailed(truncate(e.getMessage(), 480));
        }
    }

    //문자열 안에서 특수 문자(")를 '로 바꿔서 안전하게 저장하는 메소드
    private String safe(String s)
    { return s == null ? "" : s.replace("\"","'"); }

    //너무 긴 문자열을 잘라서 DB 컬럼 길이 초과 에러 방지하는 메소드
    private String truncate(String s, int max)
    { return s == null ? null : (s.length() <= max ? s : s.substring(0, max)); }

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
                    "QuestionAlreadyExistsException",
                    "해당 도서에 대한 질문이 이미 존재합니다.");
        }
    }
}
