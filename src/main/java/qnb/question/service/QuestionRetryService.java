package qnb.question.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qnb.common.exception.InvalidStatusException;
import qnb.common.exception.QuestionNotFoundException;
import qnb.question.entity.Question;
import qnb.question.event.QuestionRetryRequestedEvent;
import qnb.question.repository.QuestionRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class QuestionRetryService {

    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 재시도 쿨다운 (60초)
    @Value("${ml.retry-cooldown-sec:60}")
    private int retryCooldownSec;

    // 최대 재시도 횟수 (3회)
    @Value("${ml.retry-max-count:3}")
    private int retryMaxCount;

    @Transactional
    public Integer retry(Integer questionId) {
        // 1) 비관적 락 + 404
        Question q = questionRepository.findByIdForUpdate(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("해당 질문을 찾을 수 없습니다."));

        // 2) 상태 검증 (FAILED 외 상태에서는 재시도 불가)
        if (q.getStatus() != Question.QuestionStatus.FAILED) {
            throw new InvalidStatusException(
                    "InvalidStatusException",
                    "현재 상태에서는 재시도할 수 없습니다."
            );
        }

        // 3) 직전 실패가 비재시도 유형이면 차단(ML_4xx/VALIDATION)
        if (isNonRetryable(q.getLastError())) {
            throw new InvalidStatusException(
                    "NonRetryableLastError",
                    "데이터/권한 문제로 재시도할 수 없습니다. (" + safeCode(q.getLastError()) + ")"
            );
        }

        // 4) 최대 재시도 횟수 제한
        if (retryMaxCount > 0 && q.getRetryCount() != null && q.getRetryCount() >= retryMaxCount) {
            throw new InvalidStatusException(
                    "RetryMaxExceeded",
                    "최대 재시도 횟수를 초과했습니다."
            );
        }

        // 5) 레이트 리밋(쿨다운)
        if (retryCooldownSec > 0 && q.getLastRetryAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime readyAt = q.getLastRetryAt().plusSeconds(retryCooldownSec);
            if (now.isBefore(readyAt)) {
                long secondsLeft = ChronoUnit.SECONDS.between(now, readyAt);
                throw new InvalidStatusException(
                        "RetryCooldownNotElapsed",
                        "재시도 간격이 너무 짧습니다. " + secondsLeft + "초 후 다시 시도해주세요.",
                        secondsLeft // <- GlobalExceptionHandler에서 Retry-After 헤더로 사용
                );
            }
        }

        // 6) FAILED -> GENERATING 상태 변경 (retryCount++, lastRetryAt=now, lastError=null)
        q.markGeneratingForRetry();

        // 7) 커밋 후 비동기 실행
        eventPublisher.publishEvent(new QuestionRetryRequestedEvent(q.getQuestionId()));
        return q.getQuestionId();
    }

    // 규칙: 4xx/검증 실패는 재시도 금지
    private boolean isNonRetryable(String lastError) {
        if (lastError == null) return false;
        return lastError.startsWith("ML_400")
                || lastError.startsWith("ML_401")
                || lastError.startsWith("ML_403")
                || lastError.startsWith("ML_404")
                || lastError.startsWith("VALIDATION");
    }

    private String safeCode(String lastError) {
        int idx = lastError.indexOf(':');
        return idx > 0 ? lastError.substring(0, idx) : lastError;
    }
}
