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
        String createdAt,
        int likeCount,
        int commentCount,
        int shareCount,
        FindMemberResponse authorResponse,
        List<String> imageUrls
) {

    public static FindPostResponse from(Post post) {
        // 이미지 URL을 PostImage 객체에서 가져와 List<String>으로 변환
        List<String> imageUrls = post.getImages().stream()
            .map(PostImage::getImageUrl)
            .collect(Collectors.toList());

        if (imageUrls.isEmpty()) {
            imageUrls = List.of(); // 빈 리스트
        }

        return new FindPostResponse(
            post.getId(),
            post.getContent(),
            formatDateTime(post.getCreatedDate()),
            post.getLikeCount(),
            post.getCommentCount(),
            post.getShareCount(),
            FindMemberResponse.from(post.getMember()),
            imageUrls // 이미지 URL 리스트 추가
        );
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }
}
