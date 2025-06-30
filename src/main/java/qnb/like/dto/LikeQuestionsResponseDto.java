package qnb.like.dto;
//"질문 여러 개 + 페이지 정보" 응답을 담는 DTO

import lombok.AllArgsConstructor;
import lombok.Getter;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class LikeQuestionsResponseDto {
    private List<LikeQuestionDto> questions;
    private PageInfoDto pageInfo;
}

