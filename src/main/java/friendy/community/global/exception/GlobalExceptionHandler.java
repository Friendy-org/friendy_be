package friendy.community.global.exception;

import friendy.community.global.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ExceptionResponse> handleFriendyException(FriendyException friendyException) {
        log.warn("[FriendyException] {}: {}", friendyException.getClass().getName(), friendyException.getMessage());

        // ExceptionResponse를 반환
        ExceptionResponse response = ExceptionResponse.of(
            friendyException.getErrorCode().getCode(),
            friendyException.getMessage()
        );

        return ResponseEntity.status(friendyException.getErrorCode().getHttpStatus())
            .body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        log.warn("[MethodArgumentNotValidException] {}: {}", exception.getClass().getName(), exception.getMessage());

        BindingResult bindingResult = exception.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors().stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .toList();

        // FriendyException 생성
        FriendyException friendyException = new FriendyException(ErrorCode.INVALID_REQUEST, String.join("\n", errorMessages));

        // ExceptionResponse 생성
        ExceptionResponse response = ExceptionResponse.of(
            friendyException.getErrorCode().getCode(),
            friendyException.getMessage()
        );

        // 예외 발생시 반환되는 응답
        return ResponseEntity.status(friendyException.getErrorCode().getHttpStatus())
            .body(response);
    }
}
