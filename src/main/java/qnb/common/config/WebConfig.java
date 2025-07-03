package qnb.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로 허용
                .allowedOrigins("http://localhost:3000",
                        "http://54.252.35.36:3000",
                        "http://192.168.0.109:3000",
                        "http://192.168.0.17:3000")
                .allowedMethods("*") // GET, POST, PUT, DELETE 등 모든 메서드 허용
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
