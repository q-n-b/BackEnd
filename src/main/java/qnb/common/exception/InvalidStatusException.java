package qnb.common.exception;

public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException() {
        super("스크랩 상태는 wish, reading, read 중 하나여야 합니다.");
    }
}
