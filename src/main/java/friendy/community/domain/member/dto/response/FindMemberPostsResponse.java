package friendy.community.domain.member.dto.response;

import java.util.List;

public record FindMemberPostsResponse(
    List<PostPreview> posts,
    boolean hasNext,
    Long lastPostId
) {
}
