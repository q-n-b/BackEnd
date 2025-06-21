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

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionService questionService;

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
        int safePage = Math.max(page - 1, 0);
        int safeSize = Math.min(Math.max(size, 1), 50); // 최소 1 ~ 최대 50


        System.out.println("📢 searchFull 진입 - 원본 page: " + page + ", size: " + size);
        System.out.println("📢 보정된 safePage: " + safePage + ", safeSize: " + safeSize);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.unsorted());

        // 로그: keyword 값 확인
        System.out.println("🔍 검색 시작 - type: " + type + ", keyword: [" + keyword + "], page: " + safePage + ", size: " + safeSize);

        if (type.equals("BOOK")) {
            System.out.println("📚 책 검색 시작");
            Page<Book> books = bookRepository.searchBooks(keyword, pageable);

            return new BookSearchResponseDto(
                    books.getContent().stream()
                            .map(book -> BookSearchOneDto.from(book, book.getScrapCount()))
                            .toList(),
                    new PageInfoDto(safePage, books.getTotalPages(), (int) books.getTotalElements())
            );

        } else if (type.equals("QUESTION")) {
            System.out.println("❓ 질문 검색 시작");

            System.out.println("🔑 keyword = [" + keyword + "]");

            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("⚠️ keyword가 공백이므로 최신 질문 목록 재사용");

                QuestionPageResponseDto recentResult = questionService.getRecentQuestions(safePage, safeSize);

                System.out.println("📦 getRecentQuestions 결과 수: " + recentResult.getQuestions().size());


                // ✅ QuestionResponseDto → QuestionSearchOneDto 변환
                List<QuestionSearchOneDto> resultList = recentResult.getQuestions().stream()
                        .map(q -> new QuestionSearchOneDto(
                                q.getQuestionId().longValue(),
                                q.getQuestionContent(),
                                new BookSimpleDto(  // BookResponseDto → BookSimpleDto 변환
                                        q.getBook().getBookId(),
                                        q.getBook().getTitle(),
                                        q.getBook().getImageUrl()
                                ),
                                q.getAnswerCount(),
                                q.getLikeCount(),
                                q.getScrapCount()
                        ))
                        .toList();

                return new QuestionSearchResponseDto(
                        resultList,
                        recentResult.getPageInfoDto()
                );
            } else {
                System.out.println("✅ keyword가 존재하므로 searchQuestions() 실행");

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
            }
        }
        else { // type == "ANSWER"
            System.out.println("📝 답변 검색 시작");
            Page<Answer> answers = answerRepository.searchAnswers(keyword, pageable);

            return new AnswerSearchResponseDto(
                    answers.getContent().stream()
                            .map(a -> new AnswerSearchOneDto(
                                    a.getAnswerId(),
                                    a.getAnswerContent(),
                                    new QuestionSimpleDto(
                                            a.getQuestion().getQuestionId().longValue(),
                                            a.getQuestion().getQuestionContent()
                                    ),
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
        if ("BOOK".equals(type)) {
            return Sort.by("title").ascending().and(Sort.by("bookId").descending());
        } else if ("popular".equals(sort)) {
            return Sort.by("likeCount").descending();
        }
        return Sort.by("createdAt").descending();
    }

}
