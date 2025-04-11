package friendy.community.domain.follow.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowExceptionCode implements ExceptionCode {

    ALREADY_FOLLOWED(4601, "이미 팔로우한 회원입니다."),
    NOT_FOLLOWED(4602, "팔로우하지 않은 회원입니다."),
    SELF_FOLLOW_NOT_ALLOWED(4603, "자기 자신을 대상으로 수행할 수 없습니다."),
    FOLLOWING_MEMBER_NOT_FOUND(4604, "팔로잉 멤버가 없습니다."),
    FOLLOWER_MEMBER_NOT_FOUND(4605, "팔로워 멤버가 없습니다.");

    private final int code;
    private final String message;
}
