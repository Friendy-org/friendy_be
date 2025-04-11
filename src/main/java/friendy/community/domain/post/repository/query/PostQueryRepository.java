package friendy.community.domain.post.repository.query;

import friendy.community.domain.post.model.Post;
import java.util.List;
import java.util.Optional;

public interface PostQueryRepository {
    Optional<Post> findPostById(Long postId);
    List<Post> findPostsByLastId(Long lastPostId, int size);
    List<Post> findPostsByMemberId(Long memberId, Long lastPostId);
}
