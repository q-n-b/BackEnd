// dto/RecommendedPick.java (조회 전용)
package qnb.recommend.dto;
import java.time.LocalDateTime;

public interface RecommendedPick {
    Long getBookId();
    Double getScore();
    LocalDateTime getCreatedAt();
}

