package friendy.community.domain.email.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailSuccessCode implements ApiCode {

    AUTH_CODE_SENT(1301, "인증코드 전송 성공"),
    AUTH_CODE_VERIFIED(1302, "인증코드 검증 성공");

    private final int code;
    private final String message;
}
