package qnb.common.exception;

public class ScrapNotFoundException extends RuntimeException {
    public ScrapNotFoundException() {
        super("해당 도서에 대한 스크랩이 존재하지 않습니다.");
    }
}
