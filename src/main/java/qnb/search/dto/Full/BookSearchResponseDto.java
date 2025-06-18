package qnb.search.dto.Full;
//책 리스트와 페이지 정보 묶은 전체 응답 DTO

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponseDto {
    private List<BookSearchOneDto> books;
    private PageInfoDto pageInfo;
}

