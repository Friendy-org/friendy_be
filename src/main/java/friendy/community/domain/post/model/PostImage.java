package friendy.community.domain.post.model;

import friendy.community.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private int imageOrder;

    public PostImage(String imageUrl, int imageOrder) {
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public static PostImage of(String imageUrl, int imageOrder) {
        return new PostImage(imageUrl, imageOrder);
    }

    public void assignPost(Post post) { this.post = post; }

    public void changeImageOrder(int newOrder) { this.imageOrder = newOrder; }
}
