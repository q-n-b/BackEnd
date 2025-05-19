package com.example.qnb.question.service;
//QuestionService 파일

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

    public void createQuestion(Long userId, String userNickname, Integer bookId, QuestionRequestDto dto) {
        Question question = new Question();
        question.setUserId(userId);
        question.setUserNickname(userNickname);
        question.setBookId(bookId);
        question.setQuestionContent(dto.getQuestionContent());
        question.setLikeCount(0);
        question.setScrapCount(0);
        question.setCreatedAt(LocalDateTime.now());

        questionRepository.save(question);
    }
}
