package com.example.qnb.answer.service;

import com.example.qnb.answer.dto.AnswerRequestDto;
import com.example.qnb.answer.dto.AnswerResponseDto;
import com.example.qnb.answer.entity.Answer;
import com.example.qnb.answer.repository.AnswerRepository;
import com.example.qnb.common.exception.UnauthorizedAccessException;
import com.example.qnb.common.exception.AnswerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public void deleteAnswer(Long answerId, Long loginUserId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(AnswerNotFoundException::new); // 404

        if (!answer.getUserId().equals(loginUserId)) {
            throw new UnauthorizedAccessException(); // 403
        }

        answerRepository.delete(answer);
    }

}