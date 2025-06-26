package qnb.common.exception;

public class TargetNotFoundException extends RuntimeException {
    public TargetNotFoundException() {
        super("해당 항목을 찾을 수 없습니다.");
    }
}
