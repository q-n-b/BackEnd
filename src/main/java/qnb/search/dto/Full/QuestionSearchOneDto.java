package qnb.search.dto.Full;
//검색된 질문 1개에 대한 정보

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.book.dto.BookSimpleDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSearchOneDto {
    private Long questionId;
    private String questionContent;
    private BookSimpleDto book;
    private int answerCount;
    private int likeCount;
    private int scrapCount;
    private String userNickname;
    private String profileUrl;
}

