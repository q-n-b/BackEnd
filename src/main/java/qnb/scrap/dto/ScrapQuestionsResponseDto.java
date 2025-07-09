package qnb.scrap.dto;
//스크랩된 질문들 조회할 때 쓰이는 dto
import lombok.AllArgsConstructor;
import lombok.Getter;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class ScrapQuestionsResponseDto {
    private List<ScrapQuestionDto> scraps;
    private PageInfoDto pageInfo;
}
