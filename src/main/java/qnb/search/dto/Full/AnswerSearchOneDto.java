package qnb.search.dto.Full;
//검색된 답변 1개에 대한 정보

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import qnb.book.dto.BookSimpleDto;
import qnb.question.dto.QuestionSimpleDto;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSearchOneDto {
    private Long answerId;
    private String answerContent;
    private QuestionSimpleDto question;
    private BookSimpleDto book;
    private int likeCount;
    private String userNickname;
    private String profileUrl;
    private String answerState;
}
