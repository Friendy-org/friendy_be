package friendy.community.domain.follow.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowSuccessCode implements ApiCode {
    FOLLOW_SUCCESS(1601, "팔로잉 성공"),
    UNFOLLOW_SUCCESS(1602, "언팔로우 성공"),
    GET_FOLLOWING_LIST_SUCCESS(1603, "팔로잉 목록 조회 성공"),
    GET_FOLLOWER_LIST_SUCCESS(1604, "팔로우 목록 조회 성공");

    private final int code;
    private final String message;
}
