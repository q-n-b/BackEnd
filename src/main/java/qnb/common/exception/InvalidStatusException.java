package qnb.common.exception;

import lombok.Getter;

@Getter
public class InvalidStatusException extends RuntimeException {

    private final String errorCode;     // 사유별 코드
    private final Long retryAfterSec;   // 쿨다운 남은 시간(없으면 null)

    // 기본 생성자 (기존 스크랩 용도 호환)
    public InvalidStatusException() {
        super("스크랩 상태는 WISH, READING, READ 중 하나여야 합니다.");
        this.errorCode = "InvalidStatusException";
        this.retryAfterSec = null;
    }

    // 메시지만 넣는 경우 (예전 방식 유지)
    public InvalidStatusException(String message) {
        super(message);
        this.errorCode = "InvalidStatusException";
        this.retryAfterSec = null;
    }

    // 코드 + 메시지
    public InvalidStatusException(String errorCode, String message) {
        super(message);
        this.errorCode = (errorCode == null ? "InvalidStatusException" : errorCode);
        this.retryAfterSec = null;
    }

    // 코드 + 메시지 + Retry-After
    public InvalidStatusException(String errorCode, String message, Long retryAfterSec) {
        super(message);
        this.errorCode = (errorCode == null ? "InvalidStatusException" : errorCode);
        this.retryAfterSec = retryAfterSec;
    }
}
