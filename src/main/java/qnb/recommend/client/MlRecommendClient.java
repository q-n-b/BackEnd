package qnb.recommend.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import qnb.recommend.dto.MlRecommendRequest;
import qnb.recommend.dto.MlRecommendResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration; // ★ 추가

@Component
@RequiredArgsConstructor
public class MlRecommendClient {

    @Value("${ml.client-timeout-sec:12}")
    private int clientTimeoutSec;

    private final WebClient mlWebClient;

    // ML 서버로 토큰을 전달(헤더 그대로)하여 호출.
    public MlRecommendResponse recommendBooks(String bearerToken, int topK, MlRecommendRequest body) {
        return mlWebClient.post()
                .uri(uri -> uri.path("/api/books/recommend")
                        .queryParam("topK", topK)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearerToken) // "Bearer xxx"
                .bodyValue(body)
                .retrieve()
                .bodyToMono(MlRecommendResponse.class)
                .timeout(Duration.ofSeconds(clientTimeoutSec))  // ★ 여기만 추가
                .block();
    }
}
