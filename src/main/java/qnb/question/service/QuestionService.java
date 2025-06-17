package qnb.question.service;
//QuestionService 파일

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import qnb.book.entity.Book;
import qnb.book.repository.BookRepository;
import qnb.common.dto.PageInfoDto;
import qnb.common.exception.BookNotFoundException;
import qnb.common.exception.QuestionNotFoundException;
import qnb.common.exception.UnauthorizedAccessException;
import qnb.question.dto.QuestionPageResponseDto;
import qnb.question.dto.QuestionResponseDto;
import qnb.user.entity.User;
import qnb.user.repository.UserRepository;
import qnb.question.dto.QuestionRequestDto;
import qnb.question.entity.Question;
import qnb.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

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

    @Value("${ml.server.url}")
    private String mlServerUrl;

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
