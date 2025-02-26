package friendy.community.domain.follow.dto.response;

import friendy.community.domain.member.model.Member;

public record FollowMemberResponse(
    Long memberId,
    String nickname,
    String imageUrl
) {
    public static FollowMemberResponse from(Member member) {
        String imageUrl = null;

        if(member.getMemberImage() != null) { imageUrl = member.getMemberImage().getImageUrl();}

        return new FollowMemberResponse(
            member.getId(),
            member.getNickname(),
            imageUrl
        );
    }
}
