package qnb.common.exception;

public class InvalidSearchModeException extends RuntimeException {
    public InvalidSearchModeException() {
        super("지원하지 않는 검색 모드입니다.");
    }
}
