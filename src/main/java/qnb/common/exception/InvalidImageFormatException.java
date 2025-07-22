package qnb.common.exception;

public class InvalidImageFormatException extends RuntimeException {
    public InvalidImageFormatException() {
        super("이미지 파일이 없거나 지원되지 않는 형식입니다.");
    }
}