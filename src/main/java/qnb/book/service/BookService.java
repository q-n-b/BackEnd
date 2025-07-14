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
import qnb.question.service.QuestionService;
import qnb.user.entity.User;
import qnb.user.entity.UserPreference;
import qnb.user.repository.*;

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
    private final UserBookWishRepository wishRepository;
    private final UserBookReadingRepository readingRepository;
    private final UserBookReadRepository readRepository;

    //도서 존재 여부 로직
    public boolean existsById(Integer bookId) {
        return bookRepository.existsById(bookId);
    }

    // 0. 개인 추천 도서 1권 조회 (추천 이유 포함)
    //ML 추천 → DB에 저장 → 이후 이력 기반 응답
    //userId를 기준으로 추천 도서 1권을 받아오는 서비스 메소드.
    public SingleRecommendedBookResponseDto getSingleRecommendedBook(Long userId) {

        // ✅ 사용자 선호 정보 조회 (User 객체를 얻기 위함)
        UserPreference userPreference = userPreferenceRepository.findByUser_UserId(userId)
                .orElseThrow(UserNotFoundException::new);

        User user = userPreference.getUser();

        // ML 요청 및 저장 (현재는 주석 처리)
    /*
    RecommendationRequestDto requestDto = new RecommendationRequestDto(
            userId,
            userPreference.getPreferredGenres(),
            userPreference.getImportantFactor()
    );

    String mlUrl = "http://16.176.183.78:8080/api/books?type=recommendations&limit=1&userId=" + userId;

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<RecommendationResponseDto> response = restTemplate.getForEntity(
            mlUrl, RecommendationResponseDto.class
    );

    RecommendationResponseDto data = response.getBody();
    Book book = bookRepository.findById(data.getBookId().intValue())
            .orElseThrow(BookNotFoundException::new);

    UserRecommendedBook rec = new UserRecommendedBook(
            user,
            book,
            data.getReason(),
            data.getKeyword()
    );
    recommendedBookRepository.save(rec);
    */

        // DB에 저장된 추천 도서 중 최근 1개 가져오기
        UserRecommendedBook rec = recommendedBookRepository.findTopByUser_UserIdOrderByRecommendedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 추천 도서가 존재하지 않습니다."));

        Book book = rec.getBook();
        String reason = rec.getReason();
        String keyword = rec.getKeyword();

        return new SingleRecommendedBookResponseDto(book, reason, keyword);
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

    //상세 조회 메소드
    public BookDetailResponseDto getBookDetail(Integer bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(BookNotFoundException::new);

        //스크랩 여부 기본값
        boolean isScrapped = false;
        String scrapStatus = null;

        //사용자 스크랩 여부 확인
        if (user != null) {
            Long userId = user.getUserId();
            Integer bId = book.getBookId();

            if (wishRepository.existsByUser_UserIdAndBook_BookId(userId, bId)) {
                isScrapped = true;
                scrapStatus = "wish";
            } else if (readingRepository.existsByUser_UserIdAndBook_BookId(userId, bId)) {
                isScrapped = true;
                scrapStatus = "reading";
            } else if (readRepository.existsByUser_UserIdAndBook_BookId(userId, bId)) {
                isScrapped = true;
                scrapStatus = "read";
            }
        }

        return BookDetailResponseDto.from(book, isScrapped, scrapStatus);
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

        BookSimpleDto bookDto = new BookSimpleDto(book.getBookId(), book.getTitle(),book.getImageUrl(),
                book.getAuthor(),book.getPublisher(),book.getPublishedYear());

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
