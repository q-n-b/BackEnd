package qnb.question.service;
//QuestionService 파일

import org.springframework.web.client.RestTemplate;
import qnb.answer.dto.AnswerListItemDto;
import qnb.answer.dto.AnswerResponseDto;
import qnb.answer.dto.AnswersByUserDto;
import qnb.answer.entity.Answer;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.common.dto.PageInfoDto;
import qnb.common.exception.BookNotFoundException;
import qnb.common.exception.QuestionNotFoundException;
import qnb.common.exception.UserNotFoundException;
import qnb.common.exception.UnauthorizedAccessException;
import qnb.like.repository.UserAnswerLikeRepository;
import qnb.like.repository.UserQuestionLikeRepository;
import qnb.question.dto.*;
import qnb.scrap.repository.UserQuestionScrapRepository;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import qnb.answer.repository.AnswerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QuestionService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;

    private final UserQuestionLikeRepository userQuestionLikeRepository;
    private final UserQuestionScrapRepository userQuestionScrapRepository;
    private final UserAnswerLikeRepository userAnswerLikeRepository;

    //질문 등록 메소드
    public Question createQuestion(Long userId, Integer bookId, QuestionRequestDto dto) {
        //user 객체 자체를 가져와서 사용
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        // book 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException());

        book.setQuestionCount(book.getQuestionCount() + 1);
        bookRepository.save(book);

        Question question = new Question();
        question.setUser(user);
        question.setBook(book);
        question.setQuestionContent(dto.getQuestionContent());
        question.setLikeCount(0);
        question.setScrapCount(0);
        question.setCreatedAt(LocalDateTime.now());

        return questionRepository.save(question);
    }

    //질문 수정 메소드
    @Transactional
    public Question updateQuestion(Integer questionId, Long userId, QuestionRequestDto dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException());

        //질문 작성자와 해당 사용자 userId 비교
        if (!question.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException();
        }

        question.setQuestionContent(dto.getQuestionContent());

        return questionRepository.save(question);
    }

    //질문 삭제 메소드
    @Transactional
    public void deleteQuestion(Integer questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(QuestionNotFoundException::new); // 404

        if (!question.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedAccessException(); // 403
        }

        Book book = bookRepository.findById(question.getBook().getBookId())
                .orElseThrow(BookNotFoundException::new);

        int currentCount = book.getQuestionCount();
        book.setQuestionCount(Math.max(0, currentCount - 1)); // 음수 방지
        bookRepository.save(book);


        questionRepository.delete(question);
    }

    // 최신 질문 조회 메소드
    public QuestionPageResponseDto getRecentQuestions(int page, int size) {
        // page가 1보다 작으면 1로 보정
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(
                safePage - 1, //스프링은 0부터 시작하니까
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt") //최신순 정렬
        );

        Page<Question> questionPage = questionRepository.findAll(pageable);

        //결과가 비었을 경우 처리
        if (questionPage.isEmpty()) {
            //빈 리스트로 반환
            List<QuestionResponseDto> questions = Collections.emptyList();

            PageInfoDto pageInfoDto = new PageInfoDto(
                    safePage,  // 클라이언트가 요청한 페이지 그대로 사용
                    questionPage.getTotalPages(),
                    questionPage.getTotalElements()
            );

            return new QuestionPageResponseDto(questions, pageInfoDto);
        }

        List<QuestionResponseDto> questions = questionPage.getContent().stream()
                .map(question -> QuestionResponseDto.from(question, 0))
                // answerCount는 0으로 고정
                .collect(Collectors.toList());

        PageInfoDto pageInfoDto = new PageInfoDto(
                // Spring의 0-based index → 1-based로 변환
                questionPage.getNumber() + 1,
                questionPage.getTotalPages(),
                questionPage.getTotalElements()
        );
        return new QuestionPageResponseDto(questions, pageInfoDto);
    }

    //질문 상세 조회 + 답변 리스트 조회
    @Transactional(readOnly = true)
    public QuestionDetailResponseDto getQuestionDetail(Long questionId, String sort, Long viewerId) {
        Question question = questionRepository.findById(Math.toIntExact(questionId))
                .orElseThrow(QuestionNotFoundException::new);

        List<Answer> answers = answerRepository.findByQuestion_QuestionId(questionId);

        // 사용자별 그룹핑/정렬 로직 (기존 그대로)
        Map<Long, List<Answer>> grouped = answers.stream()
                .filter(a -> a.getUser() != null)
                .collect(Collectors.groupingBy(a -> a.getUser().getUserId()));

        Map<String, Integer> stateOrder = Map.of("BEFORE", 0, "READING", 1, "AFTER", 2);

        Comparator<Answer> innerComparator = Comparator.comparing(
                (Answer a) -> stateOrder.getOrDefault(
                        Optional.ofNullable(a.getAnswerState()).orElse("").toUpperCase(), 99)
        );

        Function<List<Answer>, Integer> maxLikes = list ->
                list.stream().map(Answer::getLikeCount).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
        Function<List<Answer>, LocalDateTime> latestCreated = list ->
                list.stream().map(Answer::getCreatedAt).filter(Objects::nonNull).max(LocalDateTime::
                                compareTo)
                        .orElse(LocalDateTime.MIN);

        Comparator<Map.Entry<Long, List<Answer>>> blockComparator =
                "popular".equalsIgnoreCase(sort)
                        ? Comparator.<Map.Entry<Long, List<Answer>>,
                                Integer>comparing(e -> maxLikes.apply(e.getValue()))
                        .reversed()
                        .thenComparing(e -> latestCreated.apply(e.getValue()),
                                Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey)
                        : Comparator.<Map.Entry<Long, List<Answer>>, LocalDateTime>comparing(
                                e -> latestCreated.apply(e.getValue()),
                                Comparator.reverseOrder())
                        .thenComparing(e -> maxLikes.apply(e.getValue()),
                                Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey);

        List<AnswersByUserDto> answersByUser = grouped.entrySet().stream()
                .sorted(blockComparator)
                .map(entry -> {
                    Long uid = entry.getKey();
                    User user = userRepository.findById(uid).orElseThrow(UserNotFoundException::new);

                    List<AnswerListItemDto> answerDtos = entry.getValue().stream()
                            .sorted(innerComparator)
                            .map(a -> {
                                boolean aLiked = false;
                                if (viewerId != null) {
                                    aLiked = userAnswerLikeRepository
                                            .existsByUser_UserIdAndAnswer_AnswerId(viewerId,
                                                    a.getAnswerId().intValue());
                                }
                                return AnswerListItemDto.of(
                                        a,
                                        user.getUserId().toString(),
                                        Optional.ofNullable(user.getUserNickname()).orElse(
                                                "알 수 없음"),
                                        Optional.ofNullable(user.getProfileUrl()).orElse(""),
                                        aLiked
                                );
                            })
                            .toList();

                    return new AnswersByUserDto(user, answerDtos);
                })
                .toList();


        // 질문 isLiked / isScrapped
        boolean qLiked = false;
        boolean qScrapped = false;
        if (viewerId != null) {
            qLiked = userQuestionLikeRepository
                    .existsByUser_UserIdAndQuestion_QuestionId(viewerId, question.getQuestionId());
            qScrapped = userQuestionScrapRepository
                    .existsByUserIdAndQuestion_QuestionId(viewerId, question.getQuestionId());
        }

        QuestionListItemDto questionDto = QuestionListItemDto.of(
                question.getQuestionId(),
                question.getBook().getBookId(),
                question.getUser() != null ? question.getUser().getUserId() : null,
                question.getUser() != null ? question.getUser().getUserNickname() : "사용자",
                question.getUser() != null ? question.getUser().getProfileUrl() : null,
                question.getQuestionContent(),
                answers.size(),                         // 상세 화면: 전체 답변 수
                question.getLikeCount(),
                question.getScrapCount(),
                question.getStatus().name(),
                question.getCreatedAt(),
                qLiked,
                qScrapped
        );

        return new QuestionDetailResponseDto(questionDto, answersByUser);
    }

}
