package friendy.community.domain.comment.repository;

import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findById(Long id);

    List<Comment> findAllByPost(Post post);

}
