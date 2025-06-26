package qnb.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponseDto {
    private String type;      // "QUESTION" or "ANSWER"
    private Long id;          // 좋아요 대상 ID
    private int likeCount;    // 현재 좋아요 수
    private boolean isLiked;  // true: 좋아요 등록됨, false: 취소됨
}

