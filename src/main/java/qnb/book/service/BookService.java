package qnb.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
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
import org.springframework.transaction.annotation.Transactional;
import qnb.user.entity.User;
import qnb.user.entity.UserPreference;
import qnb.user.repository.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.sql.Date;

import java.sql.ResultSet;
import java.sql.SQLException;

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

    public boolean existsById(Integer bookId) {
        return bookRepository.existsById(bookId.intValue());
    }

    // 주간 확정 테이블 접근용 (새 레포지토리 안 만들고 JdbcTemplate 활용)
    private final JdbcTemplate jdbcTemplate;

    private LocalDate thisMondayKST() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @Transactional(readOnly = true)
    public SingleRecommendedBookResponseDto getOneRecommendedBook(Long userId) {
        // 1) 이번 주 확정(weekly) 먼저 시도
        LocalDate weekStart = thisMondayKST();
        Integer weeklyBookId = jdbcTemplate.query(
                """
                SELECT book_id
                FROM user_weekly_featured_book
                WHERE user_id = ? AND week_start = ?
                """,
                ps -> { ps.setLong(1, userId);
                    ps.setDate(2, Date.valueOf(weekStart));
                    },
                rs -> rs.next() ? rs.getInt("book_id") : null
        );

        if (weeklyBookId != null) {
            Book book = bookRepository.findById(weeklyBookId)
                    .orElseThrow(BookNotFoundException::new);

            // 같은 유저 + 같은 책의 추천 기록 중 최신 1개를 찾아 keyword 가져오기
            String keyword = recommendedBookRepository
                    .findTopByUser_UserIdAndBook_BookIdOrderByRecommendedAtDesc(userId, weeklyBookId)
                    .map(UserRecommendedBook::getKeyword)
                    .orElse(null);

            return new SingleRecommendedBookResponseDto(book, keyword);
        }

        // 2) 주간 확정이 없으면 최신 1개(추천 풀)로 폴백
        UserRecommendedBook rec = recommendedBookRepository
                .findTopByUser_UserIdOrderByRecommendedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("추천 도서가 없습니다."));

        return new SingleRecommendedBookResponseDto(rec.getBook(), rec.getKeyword());
    }

    // 개인 추천 도서 리스트 조회
    public List<BookResponseDto> getRecommendedBooks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<UserRecommendedBook> list = recommendedBookRepository.findAllByUserOrderByRecommendedAtDesc(user);

        return list.stream()
                .map(rec -> new BookResponseDto(rec.getBook()))
                .toList();
    }


    //  장르별 추천 도서 리스트 조회
    public List<BookResponseDto> getRecommendedBooksByGenre(String genre) {
        List<UserRecommendedBook> list = recommendedBookRepository.findByBookGenre(genre);
        return list.stream()
                .map(rec -> new BookResponseDto(rec.getBook()))
                .toList();
    }

    // 신간 도서 리스트 조회 (출간일 내림차순)
    public Page<BookResponseDto> getNewBooks(Pageable pageable) {
        return bookRepository.findAllByOrderByPublishedYearDesc(pageable)
                .map(BookResponseDto::new);
    }

    // 도서 상세 조회 메소드
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
                scrapStatus = "WISH";
            } else if (readingRepository.existsByUser_UserIdAndBook_BookId(userId, bId)) {
                isScrapped = true;
                scrapStatus = "READING";
            } else if (readRepository.existsByUser_UserIdAndBook_BookId(userId, bId)) {
                isScrapped = true;
                scrapStatus = "READ";
            }
        }

        return BookDetailResponseDto.from(book, isScrapped, scrapStatus);
    }


    // 특정 도서의 질문 리스트 조회
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
