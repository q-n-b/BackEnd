package qnb.like.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponseDto {
    private String type;      // "QUESTION" or "ANSWER"
    private Long id;          // 좋아요 대상 ID
    private int likeCount;    // 현재 좋아요 수

    private boolean liked;

    @JsonProperty("isLiked")  // JSON 응답에서만 "isLiked"로 보이게 함
    public boolean isLiked() {
        return liked;
    }
}

