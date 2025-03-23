package friendy.community.global.exception.domain;

import friendy.community.global.exception.dto.ExceptionCode;

public class BadGatewayException extends BusinessException {

    public BadGatewayException(ExceptionCode exceptionType) {
        super(exceptionType);
    }

}
