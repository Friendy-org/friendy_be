package friendy.community.domain.post.repository;

import friendy.community.domain.post.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage>  findByPostIdOrderByImageOrderAsc(Long postId);
}
