package friendy.community.domain.comment.dto.response;

import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.post.dto.response.FindMemberResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record FindCommentResponse(
        Long id,
        String content,
        String createdAt,
        int likeCount,
        int replyCount,
        FindMemberResponse authorResponse
) {

    public static FindCommentResponse from(Comment comment) {
        return new FindCommentResponse(
                comment.getId(),
                comment.getContent(),
                formatDateTime(comment.getCreatedDate()),
                comment.getLikeCount(),
                comment.getReplyCount(),
                FindMemberResponse.from(comment.getMember())
        );
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return null;

        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

}
