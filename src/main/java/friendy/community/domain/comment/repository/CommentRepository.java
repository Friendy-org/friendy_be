package friendy.community.domain.comment.repository;

import friendy.community.domain.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
