package friendy.community.domain.member.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements ApiCode {

    SIGN_UP_SUCCESS(1101, "회원가입에 성공했습니다."),
    CHANGE_PASSWORD_SUCCESS(1102, "비밀번호 변경에 성공했습니다."),
    GET_MEMBER_INFO_SUCCESS(1103, "프로필 조회에 성공했습니다.");

    private final int code;
    private final String message;
}
