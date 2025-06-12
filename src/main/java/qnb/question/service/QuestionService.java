package com.example.qnb.question.service;
//QuestionService 파일

import com.example.qnb.book.entity.Book;
import com.example.qnb.book.repository.BookRepository;
import com.example.qnb.common.dto.PageInfo;
import com.example.qnb.common.exception.*;
import com.example.qnb.question.dto.QuestionPageResponseDto;
import com.example.qnb.question.dto.QuestionResponseDto;
import com.example.qnb.user.entity.User;
import com.example.qnb.user.repository.UserRepository;
import com.example.qnb.question.dto.QuestionRequestDto;
import com.example.qnb.question.entity.Question;
import com.example.qnb.question.repository.QuestionRepository;
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

        PageInfo pageInfo = new PageInfo(
                questionPage.getNumber() + 1,  // 1-based
                questionPage.getTotalPages(),
                questionPage.getTotalElements()
        );

        return new QuestionPageResponseDto(questions, pageInfo);
    }



}
