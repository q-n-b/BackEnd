package com.example.qnb.common.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException() {
        super("해당 질문에 대한 수정 권한이 없습니다.");
    }
}
