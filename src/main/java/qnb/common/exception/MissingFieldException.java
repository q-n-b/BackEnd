package qnb.common.exception;

public class MissingFieldException extends RuntimeException {

    // 메세지를 보내야할 때
    public MissingFieldException(String message) {
        super(message);
    }

    // 기본 생성자도 필요하면 유지
    public MissingFieldException() {
        super("필수 입력값이 누락되었습니다.");
    }
}
