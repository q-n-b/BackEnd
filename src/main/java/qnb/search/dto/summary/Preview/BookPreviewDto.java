package qnb.search.dto.summary.Preview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookPreviewDto {
    private Long bookId;
    private String title;
    private String author;
    private String genre;
}