package friendy.community.global.exception.domain;

import friendy.community.global.exception.dto.ExceptionCode;

public class UnAuthorizedException extends BusinessException {

    public UnAuthorizedException(ExceptionCode exceptionType) {
        super(exceptionType);
    }

}
