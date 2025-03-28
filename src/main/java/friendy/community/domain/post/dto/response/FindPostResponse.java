package friendy.community.domain.post.dto.response;

import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.model.PostImage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public record FindPostResponse(
        Long id,
        String content,
        String location,
        String createdAt,
        int likeCount,
        int commentCount,
        int shareCount,
        FindMemberResponse authorResponse,
        List<String> imageUrls,
        Boolean me
) {

    public static FindPostResponse from(Post post, Boolean me) {
        List<String> imageUrls = post.getImages().stream()
            .map(PostImage::getImageUrl)
            .collect(Collectors.toList());

        if (imageUrls.isEmpty()) {
            imageUrls = List.of();
        }

        return new FindPostResponse(
            post.getId(),
            post.getContent(),
            post.getLocation(),
            formatDateTime(post.getCreatedDate()),
            post.getLikeCount(),
            post.getCommentCount(),
            post.getShareCount(),
            FindMemberResponse.from(post.getMember()),
            imageUrls, 
            me
        );
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}
