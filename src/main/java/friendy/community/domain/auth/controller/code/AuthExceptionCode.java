package friendy.community.domain.auth.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthExceptionCode implements ExceptionCode {

    LOGIN_FAILED_EXCEPTION(4101, "로그인에 실패하였습니다."),
    ACCESS_TOKEN_EXTRACTION_FAILED(4102, "access 토큰 추출 실패"),
    REFRESH_TOKEN_EXTRACTION_FAILED(4103, "refresh 토큰 추출 실패"),
    ACCESS_TOKEN_EMAIL_MISSING(4104, "인증 실패(JWT 액세스 토큰 Payload 이메일 누락)"),
    REFRESH_TOKEN_EMAIL_MISSING(4105, "인증 실패(JWT 리프레시 토큰 Payload 이메일 누락)"),
    MALFORMED_ACCESS_TOKEN(4106, "잘못된 JWT 액세스 토큰 형식입니다."),
    UNSUPPORTED_ACCESS_TOKEN(4107, "지원하지 않는 JWT 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(4108, "만료된 JWT 액세스 토큰입니다."),
    EMPTY_ACCESS_TOKEN(4109, "JWT 액세스 토큰이 비어 있거나 잘못되었습니다."),
    INVALID_ACCESS_TOKEN(4110, "JWT 액세스 토큰 검증 중 알 수 없는 오류가 발생했습니다."),
    INVALID_REFRESH_TOKEN(4111, "잘못된 리프레시 토큰"),
    EXPIRED_REFRESH_TOKEN(4112, "만료된 리프레시 토큰"),
    USER_NOT_LOGGED_IN(4113, "사용자가 로그인되지 않았습니다."),
    EMAIL_NOT_REGISTERED(4114,"이메일이 존재하지 않습니다."),
    INVALID_PASSWORD(4115, "비밀번호가 올바르지 않습니다.");

    private final int code;
    private final String message;
}
