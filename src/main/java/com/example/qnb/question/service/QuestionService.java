package com.example.qnb.question.service;
//QuestionService 파일

import com.example.qnb.login.entity.User;
import com.example.qnb.login.repository.UserRepository;
import com.example.qnb.question.dto.QuestionRequestDto;
import com.example.qnb.question.entity.Question;
import com.example.qnb.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public Question createQuestion(Long userId, Integer bookId, QuestionRequestDto dto) {
        //user 객체 자체를 가져와서 사용
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        Question question = new Question();
        question.setUser(user);
        question.setBookId(bookId);
        question.setQuestionContent(dto.getQuestionContent());
        question.setLikeCount(0);
        question.setScrapCount(0);
        question.setCreatedAt(LocalDateTime.now());

        return questionRepository.save(question);
    }
}
