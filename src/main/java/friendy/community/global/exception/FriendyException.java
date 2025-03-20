package friendy.community.global.exception;

import friendy.community.global.response.ExceptionResponse;
import lombok.Getter;

@Getter
public class FriendyException extends RuntimeException {

    private final ErrorCode errorCode;

    public FriendyException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExceptionResponse toExceptionResponse() {
        return ExceptionResponse.of(errorCode.getCode(), getMessage());
    }

}
