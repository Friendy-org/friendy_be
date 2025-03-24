package friendy.community.domain.email.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailExceptionCode implements ExceptionCode {

    EMAIL_SEND_FAILURE(4301, "이메일 전송에 실패했습니다."),
    AUTH_CODE_NOT_FOUND(4302, "인증번호가 존재하지 않습니다."),
    AUTH_CODE_MISMATCH(4303, "인증번호가 일치하지 않습니다.");

    private final int code;
    private final String message;
}
