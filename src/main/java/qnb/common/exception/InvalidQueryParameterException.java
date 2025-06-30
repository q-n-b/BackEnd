package qnb.common.exception;

public class InvalidQueryParameterException extends RuntimeException {
  public InvalidQueryParameterException() {
    super("유효하지 않은 요청 파라미터입니다.");
  }

  public InvalidQueryParameterException(String message) {
    super(message);
  }
}
