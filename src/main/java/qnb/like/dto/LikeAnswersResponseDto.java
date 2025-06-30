package qnb.like.dto;
//"답변 여러 개 + 페이지 정보" 응답을 담는 DTO

import lombok.AllArgsConstructor;
import lombok.Getter;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Getter
@AllArgsConstructor
public class LikeAnswersResponseDto {
    private Likes likes;
    private PageInfoDto pageInfo;

    @Getter
    @AllArgsConstructor
    public static class Likes {
        private List<LikeAnswerDto> answers;
    }
}


