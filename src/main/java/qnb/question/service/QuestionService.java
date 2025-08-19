package qnb.question.service;
//QuestionService 파일

import org.springframework.web.client.RestTemplate;
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
import qnb.question.dto.QuestionDetailResponseDto;
import qnb.question.dto.QuestionPageResponseDto;
import qnb.question.dto.QuestionResponseDto;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import qnb.question.dto.QuestionRequestDto;
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
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QuestionService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;


    private final RestTemplate restTemplate;

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

    public QuestionDetailResponseDto getQuestionDetail(Long questionId, String sort) {
        Question question = questionRepository.findById(Math.toIntExact(questionId))
                .orElseThrow(QuestionNotFoundException::new);

        // 모든 답변 조회
        List<Answer> answers = answerRepository.findByQuestion_QuestionId(questionId);

        // 사용자별 그룹핑
        Map<Long, List<Answer>> grouped = answers.stream()
                .filter(a -> a.getUser() != null)
                .collect(Collectors.groupingBy(a -> a.getUser().getUserId()));

        // 상태 우선순위
        Map<String, Integer> stateOrder = Map.of(
                "BEFORE", 0,
                "READING", 1,
                "AFTER", 2
        );

        // 블록 내부 정렬 (상태 우선, 그다음 작성일)
        Comparator<Answer> innerComparator = Comparator
                .comparing((Answer a) -> {
                    String s = a.getAnswerState();
                    return s != null ? stateOrder.getOrDefault(s.toUpperCase(), 99) : 99;
                })
                .thenComparing(Answer::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));

        // 블록 간 정렬
        Comparator<Map.Entry<Long, List<Answer>>> blockComparator;
        if ("popular".equalsIgnoreCase(sort)) {
            // 인기순: 각 사용자 답변 중 최대 likeCount 기준 내림차순
            blockComparator = Comparator.comparing(
                    (Map.Entry<Long, List<Answer>> e) ->
                            e.getValue().stream()
                                    .map(Answer::getLikeCount)
                                    .filter(Objects::nonNull)
                                    .max(Integer::compareTo)
                                    .orElse(0)
            ).reversed();
        } else {
            // 최신순: 각 사용자 답변 중 가장 최신 createdAt 기준 내림차순
            blockComparator = Comparator.comparing(
                    (Map.Entry<Long, List<Answer>> e) ->
                            e.getValue().stream()
                                    .map(Answer::getCreatedAt)
                                    .filter(Objects::nonNull)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(LocalDateTime.MIN)
            ).reversed();
        }

        // 최종 사용자 블록 생성
        List<AnswersByUserDto> answersByUser = grouped.entrySet().stream()
                .sorted(blockComparator) // 블록 간 정렬
                .map(entry -> {
                    Long userId = entry.getKey();
                    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

                    List<AnswerResponseDto> answerDtos = entry.getValue().stream()
                            .sorted(innerComparator) // 블록 내부 정렬
                            .map(a -> AnswerResponseDto.from(
                                    a,
                                    user.getUserId().toString(),
                                    user.getUserNickname() != null ? user.getUserNickname() : "알 수 없음",
                                    user.getProfileUrl() != null ? user.getProfileUrl() : ""
                            ))
                            .collect(Collectors.toList());

                    return new AnswersByUserDto(user, answerDtos);
                })
                .collect(Collectors.toList());

        QuestionResponseDto questionDto = QuestionResponseDto.from(question, answers.size());
        return new QuestionDetailResponseDto(questionDto, answersByUser);
    }
}
