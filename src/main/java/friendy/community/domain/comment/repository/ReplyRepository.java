package friendy.community.domain.comment.repository;

import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.model.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Optional<Reply> findById(Long id);

    List<Reply> findAllByComment(Comment comment);

}
