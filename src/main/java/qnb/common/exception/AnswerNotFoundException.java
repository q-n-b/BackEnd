package com.example.qnb.common.exception;

public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException() {
        super("해당 답변을 찾을 수 없습니다.");
    }
}
