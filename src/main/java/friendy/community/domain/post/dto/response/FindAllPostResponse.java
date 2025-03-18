package friendy.community.domain.post.dto.response;

import java.util.List;

public record FindAllPostResponse(
        List<FindPostResponse> posts,
        boolean hasNext,
        Long lastPostId
) {
}
