package friendy.community.domain.comment.dto;

import java.util.List;

public record FindAllReplyResponse(
        List<FindReplyResponse> replies,
        boolean hasNext,
        Long lastPostId
) {
}
