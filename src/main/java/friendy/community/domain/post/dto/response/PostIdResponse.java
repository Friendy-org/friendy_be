package friendy.community.domain.post.dto.response;

import friendy.community.domain.post.model.Post;

public record PostIdResponse(
    Long id
) {
    public static PostIdResponse from(Post post) {
        return new PostIdResponse(post.getId());
    }
}
