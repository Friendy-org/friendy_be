package friendy.community.domain.comment.dto;

import friendy.community.domain.comment.model.Reply;
import friendy.community.domain.post.dto.response.FindMemberResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record FindReplyResponse(
        Long id,
        String content,
        String createdAt,
        int likeCount,
        FindMemberResponse authorResponse
) {
    public static FindReplyResponse from(Reply reply) {
        return new FindReplyResponse(
                reply.getId(),
                reply.getContent(),
                formatDateTime(reply.getCreatedDate()),
                reply.getLikeCount(),
                FindMemberResponse.from(reply.getMember())
        );
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}