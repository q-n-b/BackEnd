package qnb.book.dto;
//ML 서버에서 응답으로 보내는 데이터

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecommendationResponseDto {
    private Long bookId;
    private String reason;   // 예: "search", "like" 등
    private String keyword;  // 예: "아포칼립스"
}
