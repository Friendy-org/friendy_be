package friendy.community.global.exception.domain;

import friendy.community.global.exception.dto.ExceptionCode;

public class ConflictException extends BusinessException {
    public ConflictException(ExceptionCode exceptionType) {
        super(exceptionType);
    }
}
