package qnb.user.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import qnb.user.event.UserBookReadAddedEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserVectorGenerateListener {

    private final RestTemplate restTemplate;

    @Value("${ml.server.url}")
    private String mlBaseUrl;

    @Async("vectorTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserBookReadAdded(UserBookReadAddedEvent event) {
        Long userId = event.getUserId();
        String url = mlBaseUrl + "/api/users/" + userId + "/generate-vector";
        try {
            restTemplate.postForEntity(url, null, Void.class);
            log.info("Vector generation triggered for user={}", userId);
        } catch (RestClientException e) {
            log.error(" Vector generation request failed for user={}: {}", userId, e.getMessage(), e);
            // 필요시 재시도/알람 로직 추가
        }
    }
}