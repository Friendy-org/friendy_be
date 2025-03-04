package friendy.community.domain.follow.dto.response;

import java.util.List;

public record FollowListResponse(
    List<FollowMemberResponse> members,
    boolean hasNext
) {
}
