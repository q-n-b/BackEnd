package qnb.question.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import qnb.book.dto.BookSimpleDto;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDetailHeaderDto {
    private Integer questionId;
    private Long userId;
    private String userNickname;
    private String profileUrl;

    private String questionContent;
    private Integer answerCount;
    private Integer likeCount;
    private Integer scrapCount;
    private String status;
    private LocalDateTime createdAt;

    private Boolean isLiked;
    private Boolean isScrapped;

    // ✅ 상세에서만 필요한 book 블록
    private BookSimpleDto book;
}
