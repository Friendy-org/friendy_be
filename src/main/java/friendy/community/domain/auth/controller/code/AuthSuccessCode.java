package friendy.community.domain.auth.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AuthSuccessCode implements ApiCode {

    LOGIN_SUCCESS(1101, "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(1102, "로그아웃에 성공했습니다."),
    TOKEN_REISSUE_SUCCESS(1103, "토큰 재발급에 성공했습니다."),
    MEMBER_WITHDRAWAL_SUCCESS(1104, "회원 탈퇴에 성공했습니다.");

    private final int code;
    private final String message;
}
