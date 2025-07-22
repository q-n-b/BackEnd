package qnb.common.exception;

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException() {
        super("이미지 파일의 크기가 너무 큽니다. 최대 5MB까지 업로드 가능합니다.");
    }
}
