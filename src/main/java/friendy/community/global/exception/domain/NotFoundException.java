package friendy.community.global.exception.domain;

import friendy.community.global.exception.dto.ExceptionCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(ExceptionCode exceptionType) {
        super(exceptionType);
    }

}
