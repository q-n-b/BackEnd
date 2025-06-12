package qnb.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로 허용
                .allowedOrigins("http://localhost:3000", "http://<프론트EC2공인IP>:3000")
                .allowedMethods("*") // GET, POST, PUT, DELETE 등 모든 메서드 허용
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
