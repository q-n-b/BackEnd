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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        questionRepository.delete(question);
    }

    //최신 질문 조회 메소드
    public QuestionPageResponseDto getRecentQuestions(int page, int size) {
        Pageable pageable = PageRequest.of(
                page-1, //Spring은 0부터 시작하므로 1 빼줌
                size,
                Sort.by(Sort.Direction.DESC, "createdAt") //최신순 정렬
        );
        Page<Question> questionPage = questionRepository.findAll(pageable);

        if (questionPage.isEmpty()) {
            throw new QuestionNotFoundException();
        }

        List<QuestionResponseDto> questions = questionPage.getContent().stream()
                .map(question -> QuestionResponseDto.from(question, 0))
                .collect(Collectors.toList());
        //이 부분에서 answeCount는 반환되지 않으므로 해당값 0으로 변환시킴

        PageInfoDto pageInfoDto = new PageInfoDto(
                questionPage.getNumber() + 1,  // 1-based
                questionPage.getTotalPages(),
                questionPage.getTotalElements()
        );

        return new QuestionPageResponseDto(questions, pageInfoDto);
    }

    //질문 상세 조회 메소드
    public QuestionDetailResponseDto getQuestionDetail(Long questionId, String sort) {
        Question question = questionRepository.findById(Math.toIntExact(questionId))
                .orElseThrow(QuestionNotFoundException::new);

        // 답변 정렬 기준 처리
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        Comparator<Answer> comparator = sort.equals("popular")
                ? Comparator.comparing(Answer::getLikeCount).reversed()
                : Comparator.comparing(Answer::getCreatedAt).reversed();

        // 질문 정보 DTO로 변환 (답변 수 포함)
        QuestionResponseDto questionDto = QuestionResponseDto.from(question, answers.size());

        // 사용자 기준으로 묶기
        Map<Long, List<Answer>> grouped = answers.stream()
                .sorted(comparator)
                .collect(Collectors.groupingBy(Answer::getUserId));

        List<AnswersByUserDto> answersByUser = new ArrayList<>();
        for (Map.Entry<Long, List<Answer>> entry : grouped.entrySet()) {
            Long userId = entry.getKey();

            // userId로 사용자 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);

            // 답변 리스트 DTO로 변환
            List<AnswerResponseDto> answerDtos = entry.getValue().stream()
                    .map(answer -> AnswerResponseDto.from(
                            answer,
                            user.getUserId().toString(),
                            user.getUserNickname(),
                            user.getProfileUrl()))
                    .collect(Collectors.toList());

            answersByUser.add(new AnswersByUserDto(user, answerDtos));
        }

        return new QuestionDetailResponseDto(questionDto, answersByUser);
    }


    /*@Value("${ml.server.url}")
    private String mlServerUrl;
*/
    //질문 생성 메소드
   /* public void generateQuestion(Integer bookId) {
        // 1. 책 정보 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));

        // 2. ML 서버로 요청 보낼 DTO 구성
        Map<String, String> requestBody = Map.of(
                "title", book.getTitle(),
                "description", book.getDescription()
        );

        // 3. ML 서버로 POST 요청
        ResponseEntity<QuestionResponse> response = restTemplate.postForEntity(
                mlServerUrl + "/generate-question",
                requestBody,
                QuestionResponse.class
        );

        // 4. 응답 값 저장
        QuestionResponse questionRes = response.getBody();

        Question question = Question.builder()
                .book(book)
                .content(questionRes.getContent())
                .user(null) // GPT 질문이면 null 또는 별도 'GPT' user
                .build();

        questionRepository.save(question);
    }*/



}
