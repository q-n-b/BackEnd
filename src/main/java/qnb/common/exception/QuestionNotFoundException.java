package qnb.common.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException() {
        super("존재하지 않는 질문입니다.");
    }

    public QuestionNotFoundException(String message) {}
}
