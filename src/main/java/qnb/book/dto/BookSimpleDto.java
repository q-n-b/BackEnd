package qnb.book.dto;

//단순히 bookId와 제목만 리턴해주는 DTO

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookSimpleDto {
    private Integer bookId;
    private String bookTitle;
}
