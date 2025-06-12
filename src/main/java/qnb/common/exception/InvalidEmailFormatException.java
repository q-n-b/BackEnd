package com.example.qnb.common.exception;

public class InvalidEmailFormatException extends RuntimeException {
    public InvalidEmailFormatException() {
        super("이메일 형식이 올바르지 않습니다.");
    }
}
