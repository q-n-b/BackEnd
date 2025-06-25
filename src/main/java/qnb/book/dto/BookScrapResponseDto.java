package qnb.book.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookScrapResponseDto {
    private Integer bookId;
    private String scrapStatus;  // "wish", "reading", "read" 또는 null
    private String message;      // "도서 스크랩 상태가 저장되었습니다." 또는 "도서 스크랩이 취소되었습니다."
}

