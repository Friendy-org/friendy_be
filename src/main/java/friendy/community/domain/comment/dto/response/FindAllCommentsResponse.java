package friendy.community.domain.comment.dto.response;

import java.util.List;

public record FindAllCommentsResponse(
        List<FindCommentResponse> comments,
        boolean hasNext,
        Long lastCommentId
) {
}
