package friendy.community.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST(1101, HttpStatus.BAD_REQUEST),

    RESOURCE_NOT_FOUND(1201, HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL(1202, HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME(1203, HttpStatus.CONFLICT),

    UNAUTHORIZED_USER(1301, HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_EMAIL(1302, HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_PASSWORD(1303, HttpStatus.UNAUTHORIZED),
    FORBIDDEN_ACCESS(1304, HttpStatus.FORBIDDEN),

    INVALID_FILE(1401, HttpStatus.BAD_REQUEST),
    FILE_IO_ERROR(1402, HttpStatus.INTERNAL_SERVER_ERROR),

    INTERNAL_SERVER_ERROR(2000, HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final HttpStatus httpStatus;
}
