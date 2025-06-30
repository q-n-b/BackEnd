package qnb.scrap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class ScrapResponseDto {
    private List<ScrapDto> scraps;
    private PageInfoDto pageInfo;
}
