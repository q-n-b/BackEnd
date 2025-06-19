package qnb.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.book.dto.BookSimpleDto;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.common.dto.PageInfoDto;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.search.dto.Full.*;
import qnb.search.dto.SummarySearchResponseDto;
import qnb.search.dto.summary.AnswerSummaryDto;
import qnb.search.dto.summary.BookSummaryDto;
import qnb.search.dto.summary.Preview.AnswerPreviewDto;
import qnb.search.dto.summary.Preview.BookPreviewDto;
import qnb.search.dto.summary.Preview.QuestionPreviewDto;
import qnb.search.dto.summary.QuestionSummaryDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    //요약 버전 생성하는 메소드
    public BookSummaryDto createBookSummary(String keyword) {
        List<Book> books = bookRepository.findBooksForSummary(keyword);

        List<BookPreviewDto> previewList = books.stream()
                .limit(5)
                .map(book -> new BookPreviewDto(
                        book.getBookId().longValue(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre()
                ))
                .toList();

        return new BookSummaryDto(books.size(), previewList);
    }

    public QuestionSummaryDto createQuestionSummary(String keyword) {
        List<Question> questions = questionRepository.findQuestionsForSummary(keyword);

        List<QuestionPreviewDto> previewList = questions.stream()
                .limit(5)
                .map(q -> new QuestionPreviewDto(
                        q.getQuestionId().longValue(),
                        q.getBook().getBookId().longValue(),
                        q.getBook().getTitle(),
                        q.getBook().getAuthor(),
                        q.getQuestionContent()
                ))
                .toList();

        return new QuestionSummaryDto(questions.size(), previewList);
    }

    public AnswerSummaryDto createAnswerSummary(String keyword) {
        List<Answer> answers = answerRepository.findAnswersForSummary(keyword);

        List<AnswerPreviewDto> previewList = answers.stream()
                .limit(5)
                .map(a -> new AnswerPreviewDto(
                        a.getAnswerId(),
                        a.getQuestion().getQuestionId().longValue(),
                        a.getQuestion().getBook().getBookId().longValue(),
                        a.getQuestion().getBook().getTitle(),
                        a.getAnswerContent()
                ))
                .toList();

        return new AnswerSummaryDto(answers.size(), previewList);
    }


    public SummarySearchResponseDto searchSummary(String keyword) {
        return new SummarySearchResponseDto(
                createBookSummary(keyword),
                createQuestionSummary(keyword),
                createAnswerSummary(keyword)
        );
    }

    // full 버전 검색하는 메소드
    public Object searchFull(String type, String keyword, int page, int size, String sort) {
        int safePage = Math.max(page, 0); // 음수 방지
        int safeSize = Math.min(Math.max(size, 1), 50); // 최소 1 ~ 최대 50

        Pageable pageable = PageRequest.of(safePage, safeSize, getSort(type, sort)); // 과보정 제거

        if (type.equals("BOOK")) {
            Page<Book> books = bookRepository.searchBooks(keyword, pageable);

            return new BookSearchResponseDto(
                    books.getContent().stream()
                            .map(book -> BookSearchOneDto.from(book, book.getScrapCount()))
                            .toList(),
                    new PageInfoDto(safePage, books.getTotalPages(), (int) books.getTotalElements())
            );

        } else if (type.equals("QUESTION")) {
            Page<Question> questions = questionRepository.searchQuestions(keyword, pageable);

            return new QuestionSearchResponseDto(
                    questions.getContent().stream()
                            .map(q -> new QuestionSearchOneDto(
                                    q.getQuestionId().longValue(),
                                    q.getQuestionContent(),
                                    new BookSimpleDto(
                                            q.getBook().getBookId(),
                                            q.getBook().getTitle(),
                                            q.getBook().getImageUrl()
                                    ),
                                    q.getAnswerCount(),
                                    q.getLikeCount(),
                                    q.getScrapCount()
                            ))
                            .toList(),
                    new PageInfoDto(safePage, questions.getTotalPages(), (int) questions.getTotalElements())
            );

        } else { // type == "ANSWER"
            Page<Answer> answers = answerRepository.searchAnswers(keyword, pageable);

            return new AnswerSearchResponseDto(
                    answers.getContent().stream()
                            .map(a -> new AnswerSearchOneDto(
                                    a.getAnswerId(),
                                    a.getAnswerContent(),
                                    new BookSimpleDto(
                                            a.getQuestion().getBook().getBookId(),
                                            a.getQuestion().getBook().getTitle(),
                                            a.getQuestion().getBook().getImageUrl()
                                    ),
                                    a.getLikeCount()
                            ))
                            .toList(),
                    new PageInfoDto(safePage, answers.getTotalPages(), (int) answers.getTotalElements())
            );
        }
    }


    private Sort getSort(String type, String sort) {
        if (type.equals("BOOK")) {
            return Sort.by("title").ascending(); // 책은 정렬 옵션 고정해도 됨
        } else if (sort.equals("popular")) {
            return Sort.by("likeCount").descending();
        }
        return Sort.by("createdAt").descending(); // 기본: 최신순
    }
}
