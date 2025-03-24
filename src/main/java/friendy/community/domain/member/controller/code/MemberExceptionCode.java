package friendy.community.domain.member.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberExceptionCode implements ExceptionCode {

    DUPLICATE_EMAIL_EXCEPTION(4101,"이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME_EXCEPTION(4102,"닉네임이 이미 존재합니다."),
    USER_NOT_FOUND_EXCEPTION(4103, "존재하지 않는 회원입니다."),
    EMAIL_NOT_FOUND_EXCEPTION(4104, "존재하지 않는 이메일입니다."),
    INVALID_ENCRYPTION_ALGORITHM(4105, "암호화 알고리즘이 잘못 명시되었습니다.");

    private final int code;
    private final String message;
}
