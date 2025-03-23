package friendy.community.global.exception.domain;


import friendy.community.global.exception.dto.ExceptionCode;

public class BadRequestException extends BusinessException {

    public BadRequestException(ExceptionCode exceptionType) {
        super(exceptionType);
    }

}
