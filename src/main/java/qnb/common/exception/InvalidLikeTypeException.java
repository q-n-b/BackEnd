package qnb.common.exception;

public class InvalidLikeTypeException extends RuntimeException {
    public InvalidLikeTypeException() {
        super("지원하지 않는 타입입니다. (QUESTION 또는 ANSWER만 허용)");
    }
}