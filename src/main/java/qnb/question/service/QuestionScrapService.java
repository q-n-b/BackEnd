package qnb.question.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import qnb.common.exception.QuestionNotFoundException;
import qnb.question.dto.QuestionScrapResponseDto;
import qnb.question.entity.Question;
import qnb.question.entity.QuestionScrap;
import qnb.question.repository.QuestionRepository;
import qnb.question.repository.QuestionScrapRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionScrapService {

    private final QuestionRepository questionRepository;
    private final QuestionScrapRepository scrapRepository;

    public QuestionScrapResponseDto toggleScrap(Long userId, Integer questionId) {
        // 질문 존재 여부 확인
        Question question = questionRepository.findById(questionId)
                .orElseThrow(QuestionNotFoundException::new);

        // 스크랩 여부 확인
        Optional<QuestionScrap> existing = scrapRepository.findByUserIdAndQuestion_QuestionId(userId, questionId);

        boolean isScrapped;
        int updatedCount;

        if (existing.isPresent()) {
            //  이미 스크랩 → 취소
            scrapRepository.delete(existing.get());
            question.decreaseScrapCount();
            isScrapped = false;
        } else {
            // 최초 스크랩 → 등록
            QuestionScrap newScrap = QuestionScrap.builder()
                    .userId(userId)
                    .question(question)
                    .build();
            scrapRepository.save(newScrap);
            question.increaseScrapCount();
            isScrapped = true;
        }

        updatedCount = question.getScrapCount();

        return new QuestionScrapResponseDto(questionId, updatedCount, isScrapped);
    }
}
