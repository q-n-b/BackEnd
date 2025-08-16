package qnb.common.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException() {
        super("질문을 등록할 책을 찾을 수 없습니다.");
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}
