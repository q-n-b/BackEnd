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
import qnb.question.repository.projection.BookQuestionCount;
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
import java.util.Map;
import java.util.stream.Collectors;

import static io.micrometer.common.util.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;
    private final UserRepository userRepository;

    //======검색 결과 Summary 버전 생성하는 메소드======
    //도서 검색 결과 요약버전 조회 메소드
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

    //질문 검색결과 요약버전 조회 메소드
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

    //답변 검색 결과 요약버전 조회 메소드
    public AnswerSummaryDto createAnswerSummary(String keyword) {
        List<Answer> answers = answerRepository.findAnswersForSummary(keyword);

        List<AnswerPreviewDto> previewList = answers.stream()
                .filter(a -> a.getQuestion() != null &&
                        a.getQuestion().getBook() != null &&
                        a.getQuestion().getQuestionId() != null &&
                        a.getQuestion().getBook().getBookId() != null)
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

    //통합 검색 요약 조회 메소드
    public SummarySearchResponseDto searchSummary(String keyword) {
        return new SummarySearchResponseDto(
                createBookSummary(keyword),
                createQuestionSummary(keyword),
                createAnswerSummary(keyword)
        );
    }

    //도서 정렬 메소드
    private Sort buildBookSort(String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            // 인기순 = scrapCount
            return Sort.by(Sort.Order.desc("scrapCount"));
        }
        // 기본은 최신순
        return Sort.by(Sort.Order.desc("publishedYear"));
    }

    //질문 정렬 메소드
    private Sort buildQuestionSort(String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            // 인기순 = likeCount + scrapCount + answerCount
            return Sort.by(
                    Sort.Order.desc("likeCount"),
                    Sort.Order.desc("scrapCount"),
                    Sort.Order.desc("createdAt")
            );
        }
        // 기본은 최신순
        return Sort.by(Sort.Order.desc("createdAt"));
    }


    //======검색 결과 FULL 버전 생성하는 메소드======
    public Object searchFull(String type, String keyword, int page, int size, String sort) {
        int safePage = Math.max(page, 1); //1부터 시작하는 페이지 번호 (최소 1), currentPage 초기값 1
        int safeSize = Math.min(Math.max(size, 1), 50); //한 페이지당 항목 개수 (1 ~ 50 사이 제한)

        //1. 책 검색 결과
        if ("BOOK".equals(type)) {
            Sort sortSpec = buildBookSort(sort);
            Pageable pageable = PageRequest.of(safePage - 1, safeSize, sortSpec);

            Page<Book> books;
            if (isBlank(keyword)) {
                books = bookRepository.findAll(pageable);
            } else {
                books = bookRepository.searchBooks(keyword, pageable);
            }

            // 1) 현재 페이지의 bookId 리스트
            List<Long> bookIds = books.getContent().stream()
                    .map(b -> b.getBookId().longValue())
                    .toList();

            // 2) 질문 수 집계 (book_id → count)
            Map<Long, Long> questionCountMap = questionRepository.countByBookIds(bookIds)
                    .stream()
                    .collect(Collectors.toMap(
                            BookQuestionCount::getBookId,
                            BookQuestionCount::getQuestionCount
                    ));

            // 3) DTO 변환
            return new BookSearchResponseDto(
                    books.getContent().stream()
                            .map(book -> {
                                int qCount = questionCountMap
                                        .getOrDefault(book.getBookId().longValue(), 0L)
                                        .intValue();
                                return BookSearchOneDto.from(
                                        book,
                                        book.getScrapCount(),
                                        qCount
                                );
                            })
                            .toList(),
                    new PageInfoDto(
                            safePage,
                            books.getTotalPages(),
                            (int) books.getTotalElements()
                    )
            );
        }

        //2. 질문 검색 결과
        else if ("QUESTION".equals(type)) {
            Sort sortSpec = buildQuestionSort(sort);
            Pageable pageable = PageRequest.of(safePage - 1, safeSize, sortSpec);

            Page<Question> questions;
            if (isBlank(keyword)) {
                //키워드 없을 때
                questions = questionRepository.findAll(pageable);
            } else {
                //키워드 있을 때
                questions = questionRepository.searchQuestions(keyword, pageable);
            }

            List<QuestionSearchOneDto> resultList = questions.getContent().stream()
                    .map(q -> {
                        int realAnswerCount = answerRepository.countByQuestion_QuestionId(
                                q.getQuestionId().longValue());

                        return new QuestionSearchOneDto(
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
                                realAnswerCount,
                                q.getLikeCount(),
                                q.getScrapCount(),
                                q.getUser().getUserNickname(),
                                q.getUser().getProfileUrl()
                        );
                    })
                    .toList();

            return new QuestionSearchResponseDto(
                    resultList,
                    new PageInfoDto(
                            safePage,
                            questions.getTotalPages(),
                            (int) questions.getTotalElements()
                    )
            );
        }

       // 3. 답변 검색 결과 (정렬 없음: 그대로 출력)
        else {
            // 답변 전용 pageable: 정렬 없이(unsorted), 페이지/사이즈만 반영
            Pageable answerPageable = PageRequest.of(safePage - 1, safeSize, Sort.unsorted());

            Page<Answer> answers;
            if (keyword == null || keyword.trim().isEmpty()) {
                answers = answerRepository.findAll(answerPageable); // 공백이면 전체
            } else {
                answers = answerRepository.searchAnswers(keyword, answerPageable); // 키워드 있으면 검색
            }

            return new AnswerSearchResponseDto(
                    answers.getContent().stream()
                            .filter(a -> a.getQuestion() != null && a.getQuestion().
                                    getBook() != null)
                            .map(a -> {
                                User user = userRepository.findById(a.getUser().getUserId())
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
                            .toList(),
                    new PageInfoDto(
                            safePage,
                            answers.getTotalPages(),
                            (int) answers.getTotalElements()
                    )
            );
        }

    }
}
