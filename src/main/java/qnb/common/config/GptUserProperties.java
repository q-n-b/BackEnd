// GPT 전용시스템 사용자
package qnb.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
public class GptUserProperties {
    private Long userId;           // 초기화 시 주입
    private String email = "gpt@system.local";
    private String nickname = "GPT";
}
