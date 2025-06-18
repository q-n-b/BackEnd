package qnb.search.dto.Full;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSearchResponseDto {
    private List<QuestionSearchOneDto> questions;
    private PageInfoDto pageInfo;
}
