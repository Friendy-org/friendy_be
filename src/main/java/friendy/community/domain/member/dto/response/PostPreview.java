package friendy.community.domain.member.dto.response;

import friendy.community.domain.post.model.Post;

public record PostPreview(
    Long id,
    String thumbnail
) {
    public static PostPreview from(Post post) {
        String image = post.getImages().isEmpty() ? null : post.getImages().get(0).getImageUrl();
        return new PostPreview(post.getId(), image);
    }
}
