package com.example.qnb.answer.dto;

import com.example.qnb.answer.entity.Answer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerResponseDto {
    private Long questionId;
    private Long answerId;
    private String userId;
    private String userNickname;
    private String profileUrl;
    private String answerContent;
    private String answerState;
    private int likeCount;
    private String createdAt;

    public AnswerResponseDto(Long questionId, Answer answer, String userId, String nickname, String profileUrl) {
        this.questionId = questionId;
        this.answerId = answer.getAnswerId();
        this.userId = userId;
        this.userNickname = nickname;
        this.profileUrl = profileUrl;
        this.answerContent = answer.getAnswerContent();
        this.answerState = answer.getAnswerState();
        this.likeCount = answer.getLikeCount();
        this.createdAt = answer.getCreatedAt().toString();
    }



}