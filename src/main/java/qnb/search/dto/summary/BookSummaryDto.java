package qnb.search.dto.summary;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import qnb.search.dto.summary.Preview.BookPreviewDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDto {
    private int count;
    private List<BookPreviewDto> preview;
}