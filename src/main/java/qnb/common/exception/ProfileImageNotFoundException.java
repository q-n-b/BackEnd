package qnb.common.exception;

public class ProfileImageNotFoundException extends RuntimeException {
  public ProfileImageNotFoundException() {
    super("삭제할 프로필 이미지가 존재하지 않습니다.");
  }

  public ProfileImageNotFoundException(String message) {
    super(message);
  }
}
