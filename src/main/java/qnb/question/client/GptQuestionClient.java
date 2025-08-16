package qnb.question.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import qnb.book.entity.Book;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class GptQuestionClient {

    // MlClientConfig에서 만든 WebClient 빈 주입
    private final WebClient mlWebClient;

    // 동기 시도 타임박스(명세 2~3초)
    private static final Duration SYNC_TIMEOUT = Duration.ofSeconds(3);

    /**
     * 책 정보를 기반으로 GPT 질문을 만들어 텍스트로 반환.
     * - 2~3초 내 응답이 오면 본문 반환
     * - 타임아웃/오류 시 null 반환(서비스에서 백그라운드 이어서 처리)
     */
    public String generateBlocking(Book book) {
        GenerateReq req = new GenerateReq(
                book.getBookId().longValue(),
                book.getTitle()
        );

        try {
            return mlWebClient.post()
                    .uri("/generate-question") // baseUrl은 MlClientConfig에서 지정됨
                    .bodyValue(req)            // JSON DTO로 안전하게 전송
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            resp -> resp.createException().flatMap(Mono::error))
                    .bodyToMono(String.class)  // ML 서버가 "질문 텍스트"를 문자열로 준다고 가정
                    .timeout(SYNC_TIMEOUT)     // 타임박스
                    .block();                  // 동기 대기
        } catch (Exception e) {
            return null; // 서비스에서 null이면 GENERATING으로 처리 후 백그라운드 계속
        }
    }

    @Data
    @AllArgsConstructor
    static class GenerateReq {
        private Long bookId;
        private String title;
    }
}
