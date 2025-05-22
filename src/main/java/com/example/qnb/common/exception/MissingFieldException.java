package com.example.qnb.common.exception;

public class MissingFieldException extends RuntimeException {
    public MissingFieldException() {
        super("필수 입력 항목이 누락되었습니다.");
    }
}
