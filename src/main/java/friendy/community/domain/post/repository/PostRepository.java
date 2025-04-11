package friendy.community.domain.post.repository;

import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.query.PostQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostQueryRepository {
}
