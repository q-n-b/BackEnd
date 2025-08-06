package qnb.common.exception;

public class ProfileDeleteFailedException extends RuntimeException {
    public ProfileDeleteFailedException() {
        super("프로필 이미지 삭제 중 오류가 발생했습니다.");
    }

    public ProfileDeleteFailedException(String message) {
        super(message);
    }
}
