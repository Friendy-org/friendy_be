package friendy.community.domain.member.dto.response;

import friendy.community.domain.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record FindMemberResponse(

    @Schema(description = "해당 프로필의 소유 여부", example = "true")
    Boolean me,

    @Schema(description = "이메일", example = "example@friendy.com")
    String email,

    @Schema(description = "닉네임", example = "복성김")
    String nickname,

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.friendy.com/images/profile.png")
    String profileImageUrl,

    @Schema(description = "팔로워 수", example = "1245")
    int followerCount,

    @Schema(description = "팔로잉 수", example = "350")
    int followingCount
) {
    public static FindMemberResponse from(Member member, boolean isMe, int followerCount, int followingCount) {
        return new FindMemberResponse(
            isMe,
            member.getEmail(),
            member.getNickname(),
            member.getMemberImage().getImageUrl(),
            followerCount,
            followingCount
        );
    }
}
