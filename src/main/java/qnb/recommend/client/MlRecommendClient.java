package qnb.recommend.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import qnb.recommend.dto.MlRecommendRequest;
import qnb.recommend.dto.MlRecommendResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class MlRecommendClient {

    private final WebClient mlWebClient;

    //ML 서버로 토큰을 전달(헤더 그대로)하여 호출.
    public MlRecommendResponse recommendBooks(String bearerToken, int topK, MlRecommendRequest body) {
        return mlWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/recommend/books")
                        .queryParam("topK", topK)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearerToken) // "Bearer xxx"
                .bodyValue(body)
                .retrieve()
                .bodyToMono(MlRecommendResponse.class)
                .block();
    }
}
