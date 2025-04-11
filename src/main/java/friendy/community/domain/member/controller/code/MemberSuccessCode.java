package friendy.community.domain.member.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberSuccessCode implements ApiCode {

    SIGN_UP_SUCCESS(1201, "회원가입에 성공했습니다."),
    CHANGE_PASSWORD_SUCCESS(1202, "비밀번호 변경에 성공했습니다."),
    GET_MEMBER_INFO_SUCCESS(1203, "프로필 조회에 성공했습니다."),
    UPDATE_PROFILE_SUCCESS(1204, "프로필 수정에 성공했습니다."),
    GET_MEMBER_POSTS_SUCCESS(1205, "회원의 게시글 조회에 성공했습니다.");

    private final int code;
    private final String message;
}
