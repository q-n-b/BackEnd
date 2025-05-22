package com.example.qnb.answer.service;

import com.example.qnb.answer.dto.AnswerRequestDto;
import com.example.qnb.answer.dto.AnswerResponseDto;
import com.example.qnb.answer.entity.Answer;
import com.example.qnb.answer.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerResponseDto registerAnswer(Long questionId, Long userId, String userNickname, String profileUrl, AnswerRequestDto dto) {
        Answer answer = Answer.builder()
                .questionId(questionId)
                .userId(userId)
                .answerContent(dto.getAnswerContent())
                .answerState(dto.getAnswerState())
                .build();

        Answer saved = answerRepository.save(answer);

        return new AnswerResponseDto(questionId, saved, userId.toString(), userNickname, profileUrl);
    }
}