package qnb.common.config;


import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class MlClientConfig {

    @Value("${ml.server.url}")
    private String mlUrl;

    @Value("${ml.timeouts.connect-ms:3000}")
    private int connectMs;

    @Value("${ml.timeouts.read-ms:5000}")
    private int readMs;

    @Bean
    public WebClient mlWebClient() {
        // Netty HttpClient에 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                // 연결 타임아웃
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectMs)
                // 응답 타임아웃
                .responseTimeout(Duration.ofMillis(readMs))
                // 읽기 타임아웃 (데이터 전송이 시작된 후 제한 시간)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readMs, TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(mlUrl) // yml의 ml.server.url 값
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
