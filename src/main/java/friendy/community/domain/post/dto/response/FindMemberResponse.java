package friendy.community.domain.post.dto.response;

import friendy.community.domain.member.model.Member;

public record FindMemberResponse(
    Long id,
    String nickname,
    String profileImageUrl
) {
    public static FindMemberResponse from(Member member) {
        String profileImage;

        if (member.getMemberImage() == null) {
            profileImage = "https://friendybucket.s3.us-east-2.amazonaws.com/defaultimages/image.png";
        } else {
            profileImage = member.getMemberImage().getImageUrl();
        }
        return new FindMemberResponse(member.getId(), member.getNickname(), profileImage);
    }
}
