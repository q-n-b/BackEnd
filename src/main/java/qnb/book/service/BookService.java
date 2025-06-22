package qnb.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import qnb.answer.entity.Answer;
import qnb.answer.repository.AnswerRepository;
import qnb.book.dto.*;
import qnb.book.entity.Book;
import qnb.book.entity.UserRecommendedBook;
import qnb.book.repository.BookRepository;
import qnb.book.repository.UserRecommendedBookRepository;
import qnb.common.dto.PageInfoDto;
import qnb.common.exception.UserNotFoundException;
import qnb.question.dto.QuestionListResponseDto;
import qnb.question.dto.QuestionResponseDto;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.common.exception.BookNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import qnb.user.entity.User;
import qnb.user.entity.UserPreference;
import qnb.user.repository.UserPreferenceRepository;
import qnb.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRecommendedBookRepository recommendedBookRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    //도서 존재 여부 로직
    public boolean existsById(Integer bookId) {
        return bookRepository.existsById(bookId);
    }

    // 0. 개인 추천 도서 1권 조회 (추천 이유 포함)
    //ML 추천 → DB에 저장 → 이후 이력 기반 응답
    //userId를 기준으로 추천 도서 1권을 받아오는 서비스 메소드.
    public SingleRecommendedBookResponseDto getSingleRecommendedBook(Long userId) {

        // 사용자 정보 조회
        UserPreference userPreference = userPreferenceRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        User user = userPreference.getUser();

        // ML 요청
        RecommendationRequestDto requestDto = new RecommendationRequestDto(
                userId,
                userPreference.getPreferredGenres(),
                userPreference.getImportantFactor()
        );

        //호출할 ML 서버의 추천 API URL
        String mlUrl = "http://16.176.183.78:8080/api/books?type=recommendations&limit=1&userId=" + userId;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RecommendationResponseDto> response = restTemplate.getForEntity(
                mlUrl, RecommendationResponseDto.class
        );

        // 추천받은 Book 조회
        RecommendationResponseDto data = response.getBody();
        Book book = bookRepository.findById(data.getBookId().intValue())
                .orElseThrow(BookNotFoundException::new);

        // 추천 기록 저장
        UserRecommendedBook rec = new UserRecommendedBook(
                user,
                book,
                data.getReason(),
                data.getKeyword()
        );
        recommendedBookRepository.save(rec);

        // 응답 반환 (추천 책 + 추천 이유)
        return new SingleRecommendedBookResponseDto(book, data.getReason(), data.getKeyword());
    }

    // 1. 개인 추천 도서 리스트 조회
    public List<BookResponseDto> getRecommendedBooks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<UserRecommendedBook> list = recommendedBookRepository.findAllByUserOrderByRecommendedAtDesc(user);

        return list.stream()
                .map(rec -> new BookResponseDto(rec.getBook()))
                .toList();
    }


    // 2. 장르별 추천 도서 리스트 조회
    public List<BookResponseDto> getRecommendedBooksByGenre(String genre) {
        List<UserRecommendedBook> list = recommendedBookRepository.findByBookGenre(genre);
        return list.stream()
                .map(rec -> new BookResponseDto(rec.getBook()))
                .toList();
    }

    // 3. 신간 도서 리스트 조회 (출간일 내림차순)
    public Page<BookResponseDto> getNewBooks(Pageable pageable) {
        return bookRepository.findAllByOrderByPublishedYearDesc(pageable)
                .map(BookResponseDto::new);
    }

    // 4. 도서 상세 조회
    public BookResponseDto getBookDetail(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);
        return new BookResponseDto(book);
    }

    // 5. 특정 도서의 질문 리스트 조회
    public QuestionListResponseDto getBookQuestions(Integer bookId, String sort, Pageable pageable) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        Page<Question> questions;
        if ("popular".equals(sort)) {
            questions = questionRepository.findWithGptTopByBookIdOrderByLikeCountDesc(bookId, pageable);
        } else {
            questions = questionRepository.findWithGptTopByBookIdOrderByCreatedAtDesc(bookId, pageable);
        }

        // 질문 ID 리스트 추출
        List<Integer> questionIds = questions.getContent().stream()
                .map(Question::getQuestionId)
                .collect(Collectors.toList());

        // 질문별 답변 수 조회 (List<Object[]> → Map<Integer, Integer>)
        List<Object[]> rawAnswerCounts = answerRepository.countAnswersByQuestionIds(questionIds);
        Map<Integer, Integer> answerCountMap = rawAnswerCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // DTO 변환 (답변 수 포함)
        Page<QuestionResponseDto> questionPage = questions.map(question -> {
            String profileUrl = question.getUser().getProfileUrl();
            int answerCount = answerCountMap.getOrDefault(question.getQuestionId(), 0);

            return new QuestionResponseDto(
                    BookResponseDto.from(question.getBook()),
                    question.getUser().getUserId(),
                    question.getQuestionId(),
                    question.getUser().getUserNickname(),
                    profileUrl,
                    question.getQuestionContent(),
                    answerCount,
                    question.getLikeCount(),
                    question.getScrapCount(),
                    question.getCreatedAt()
            );
        });

        BookSimpleDto bookDto = new BookSimpleDto(book.getBookId(), book.getTitle(),book.getImageUrl());

        PageInfoDto pageInfo = new PageInfoDto(
                questions.getNumber() + 1,
                questions.getTotalPages(),
                questions.getTotalElements()
        );

        return new QuestionListResponseDto(
                bookDto,
                questionPage.getContent(),
                pageInfo
        );
    }
}
