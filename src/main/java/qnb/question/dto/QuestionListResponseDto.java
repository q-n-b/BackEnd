package qnb.question.dto;
//도서별 질문 조회 API에서 book, 질문리스트,pageinfo 반환하는 DTO

import lombok.Data;
import qnb.book.dto.BookSimpleDto;
import qnb.common.dto.PageInfoDto;

import java.util.List;

@Data
public class QuestionListResponseDto {
    private BookSimpleDto book;
    private List<QuestionListItemDto> questions;
    private PageInfoDto pageInfo;

    public QuestionListResponseDto(BookSimpleDto book, List<QuestionListItemDto> questions, PageInfoDto pageInfo) {
        this.book = book;
        this.questions = questions;
        this.pageInfo = pageInfo;
    }
}