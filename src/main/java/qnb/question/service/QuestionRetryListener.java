// qnb/question/service/QuestionRetryListener.java
package qnb.question.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import qnb.question.event.QuestionRetryRequestedEvent;

@Component
@RequiredArgsConstructor
public class QuestionRetryListener {

    private final QuestionGenerationService generationService;

    // 커밋 이후에 호출 → 내부에서 @Async("gptWorker")로 백그라운드 처리됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(QuestionRetryRequestedEvent event) {
        // 커밋 후 비동기 처리 시작
        generationService.continueInBackground(event.questionId());
    }
}
