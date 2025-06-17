package qnb.common.handler;

import qnb.common.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //이메일 형식 유효하지 않을 때
    @ExceptionHandler(InvalidEmailFormatException.class)
    public ResponseEntity<?> handleInvalidEmailFormat(InvalidEmailFormatException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "INVALID_EMAIL",
                "errorMessage", ex.getMessage()
        ));
    }

    //이메일이 이미 존재할 때
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "errorCode", "EMAIL_ALREADY_EXISTS",
                "errorMessage", ex.getMessage()
        ));
    }

    //confirmPassword에서 비밀번호 일치하지 않을 때
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<?> handlePasswordMismatch(PasswordMismatchException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "PASSWORD_MISMATCH",
                "errorMessage", ex.getMessage()
        ));
    }

    //필수 입력 항목 누락
    @ExceptionHandler(MissingFieldException.class)
    public ResponseEntity<?> handleMissingField(MissingFieldException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MISSING_FIELD",
                "errorMessage", ex.getMessage()
        ));
    }

    //비번 또는 이메일이 올바르지 않을 때
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "errorCode", "INVALID_CREDENTIALS",
                "errorMessage", ex.getMessage()
        ));
    }

    //답변이 없을 때
    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<?> handleAnswerNotFound(AnswerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "errorCode", "ANSWER_NOT_FOUND",
                        "errorMessage", ex.getMessage()
                )
        );
    }

    //질문 찾을 수 없을 때
    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<?> handleNotFound(QuestionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "errorCode", "QUESTION_NOT_FOUND",
                "errorMessage", ex.getMessage()
        ));
    }

    //해당 책을 찾을 수 없을 때
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<?> handleBookNotFound(BookNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "errorCode", "BOOK_NOT_FOUND",
                "errorMessage", ex.getMessage()
        ));
    }

    //사용자를 찾을 수 없을 때
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", "USER_NOT_FOUND");
        response.put("errorMessage", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    //권한이 없을 경우
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "errorCode", "UNAUTHORIZED",
                "errorMessage", ex.getMessage()
        ));
    }

    //질문 내용이 유효한 경우가 아닌 경우
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MISSING_QUESTION_CONTENT",
                "errorMessage", errorMessage
        ));
    }

    //로그인 필요할 시
    @ExceptionHandler(LoginRequiredException.class)
    public ResponseEntity<?> handleLoginRequired(LoginRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "errorCode", "UNAUTHORIZED",
                "errorMessage", ex.getMessage()
        ));
    }

    //서버 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleServerError(Exception ex) {
        ex.printStackTrace(); // 디버깅용
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "errorCode", "INTERNAL_ERROR",
                "errorMessage", "서버 에러가 발생했습니다."
        ));
    }

    //접근 권한 부족
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "errorCode", "FORBIDDEN",
                "errorMessage", "접근 권한이 없습니다."
        ));
    }

    //내가 남긴 Q&A 없을 때
    @ExceptionHandler(QnaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleQnaNotFound(QnaNotFoundException ex) {
        return ResponseEntity.status(404).body(
                Map.of(
                        "errorCode", "QNA_NOT_FOUND",
                        "errormessage", "남긴 Q&A가 없습니다",
                        "timestamp", ZonedDateTime.now()
                )
        );
    }
}
