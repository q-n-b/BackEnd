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
import qnb.common.exception.UserNotFoundException;
import qnb.question.dto.QuestionPageResponseDto;
import qnb.question.dto.QuestionSimpleDto;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.question.service.QuestionService;
import qnb.search.dto.Full.*;
import qnb.search.dto.SummarySearchResponseDto;
import qnb.search.dto.summary.AnswerSummaryDto;
import qnb.search.dto.summary.BookSummaryDto;
import qnb.search.dto.summary.Preview.AnswerPreviewDto;
import qnb.search.dto.summary.Preview.BookPreviewDto;
import qnb.search.dto.summary.Preview.QuestionPreviewDto;
import qnb.search.dto.summary.QuestionSummaryDto;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;

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

    //----------------------------------------------------------
    // full 버전 검색하는 메소드
    public Object searchFull(String type, String keyword, int page, int size, String sort) {
        int safePage = Math.max(page, 1); //1부터 시작하는 페이지 번호 (최소 1), currentPage 초기값 1
        int safeSize = Math.min(Math.max(size, 1), 50); //한 페이지당 항목 개수 (1 ~ 50 사이 제한)

        Pageable pageable = PageRequest.of(
                safePage - 1, //스프링은 0부터 시작하니까
                safeSize,
                Sort.unsorted());

        //1. 책 검색 결과
        if (type.equals("BOOK")) {
            Page<Book> books = bookRepository.searchBooks(keyword, pageable);

            return new BookSearchResponseDto(
                    books.getContent().stream()
                            .map(book -> BookSearchOneDto.from(book, book.getScrapCount()))
                            .toList(),
                    new PageInfoDto(
                            safePage,
                            books.getTotalPages(),
                            (int) books.getTotalElements()
                    )
            );

        }

        //2. 질문 검색 결과
        else if (type.equals("QUESTION")) {
            //키워드 없을 때
            if (keyword == null || keyword.trim().isEmpty()) {
                QuestionPageResponseDto recentResult = questionService.getRecentQuestions(safePage, safeSize);

                List<QuestionSearchOneDto> resultList = recentResult.getQuestions().stream()
                        .map(q -> new QuestionSearchOneDto(
                                q.getQuestionId().longValue(),
                                q.getQuestionContent(),
                                new BookSimpleDto(  // BookResponseDto → BookSimpleDto 변환
                                        q.getBook().getBookId(),
                                        q.getBook().getTitle(),
                                        q.getBook().getImageUrl(),
                                        q.getBook().getAuthor(),
                                        q.getBook().getPublisher(),
                                        q.getBook().getPublishedYear()
                                ),
                                q.getAnswerCount(),
                                q.getLikeCount(),
                                q.getScrapCount(),
                                q.getUserNickname(),
                                q.getProfileUrl()
                        ))
                        .toList();

                return new QuestionSearchResponseDto(
                        resultList,
                        recentResult.getPageInfoDto()
                );
            }

            //키워드 존재할 때
            else {
                Page<Question> questions = questionRepository.searchQuestions(keyword, pageable);

                return new QuestionSearchResponseDto(
                        questions.getContent().stream()
                                .map(q -> new QuestionSearchOneDto(
                                        q.getQuestionId().longValue(),
                                        q.getQuestionContent(),
                                        new BookSimpleDto(
                                                q.getBook().getBookId(),
                                                q.getBook().getTitle(),
                                                q.getBook().getImageUrl(),
                                                q.getBook().getAuthor(),
                                                q.getBook().getPublisher(),
                                                q.getBook().getPublishedYear()

                                        ),
                                        q.getAnswerCount(),
                                        q.getLikeCount(),
                                        q.getScrapCount(),
                                        q.getUser().getUserNickname(),
                                        q.getUser().getProfileUrl()
                                ))
                                .toList(),
                        new PageInfoDto(
                                safePage,
                                questions.getTotalPages(),
                                (int) questions.getTotalElements())
                );
            }
        }

        //3. 답변 검색 결과
        else if (type.equals("ANSWER")) {
            Page<Answer> answers;

            if (keyword == null || keyword.trim().isEmpty()) {
                // 🔹 키워드 없을 때: 전체 조회
                answers = answerRepository.findAll(pageable);

                List<AnswerSearchOneDto> resultList = answers.getContent().stream()
                        .map(a -> {
                            User user = userRepository.findById(a.getUserId())
                                    .orElseThrow(UserNotFoundException::new);

                            QuestionSimpleDto questionDto = null;
                            BookSimpleDto bookDto = null;

                            if (a.getQuestion() != null) {
                                questionDto = new QuestionSimpleDto(
                                        a.getQuestion().getQuestionId().longValue(),
                                        a.getQuestion().getQuestionContent()
                                );

                                if (a.getQuestion().getBook() != null) {
                                    bookDto = new BookSimpleDto(
                                            a.getQuestion().getBook().getBookId(),
                                            a.getQuestion().getBook().getTitle(),
                                            a.getQuestion().getBook().getImageUrl(),
                                            a.getQuestion().getBook().getAuthor(),
                                            a.getQuestion().getBook().getPublisher(),
                                            a.getQuestion().getBook().getPublishedYear()
                                    );
                                }
                            }

                            return new AnswerSearchOneDto(
                                    a.getAnswerId(),
                                    a.getAnswerContent(),
                                    questionDto,
                                    bookDto,
                                    a.getLikeCount(),
                                    user.getUserNickname(),
                                    user.getProfileUrl(),
                                    a.getAnswerState()
                            );
                        })
                        .toList();

                return new AnswerSearchResponseDto(
                        resultList,
                        new PageInfoDto(
                                safePage,
                                answers.getTotalPages(),
                                (int) answers.getTotalElements()
                        )
                );
            }


            // 🔹 키워드 있을 때: 기존 searchAnswers 쿼리 사용
            else {
                answers = answerRepository.searchAnswers(keyword, pageable);

                List<AnswerSearchOneDto> resultList = answers.getContent().stream()
                        .filter(a -> a.getQuestion() != null && a.getQuestion().getBook() != null)
                        .map(a -> {
                            User user = userRepository.findById(a.getUserId())
                                    .orElseThrow(UserNotFoundException::new);

                            return new AnswerSearchOneDto(
                                    a.getAnswerId(),
                                    a.getAnswerContent(),
                                    new QuestionSimpleDto(
                                            a.getQuestion().getQuestionId().longValue(),
                                            a.getQuestion().getQuestionContent()
                                    ),
                                    new BookSimpleDto(
                                            a.getQuestion().getBook().getBookId(),
                                            a.getQuestion().getBook().getTitle(),
                                            a.getQuestion().getBook().getImageUrl(),
                                            a.getQuestion().getBook().getAuthor(),
                                            a.getQuestion().getBook().getPublisher(),
                                            a.getQuestion().getBook().getPublishedYear()
                                    ),
                                    a.getLikeCount(),
                                    user.getUserNickname(),
                                    user.getProfileUrl(),
                                    a.getAnswerState()
                            );
                        })
                        .toList();

                return new AnswerSearchResponseDto(
                        resultList,
                        new PageInfoDto(
                                safePage,
                                answers.getTotalPages(),
                                (int) answers.getTotalElements()
                        )
                );
            }
        }
        else {
            throw new IllegalArgumentException("지원하지 않는 검색 타입입니다: " + type);
        }
    }

}
