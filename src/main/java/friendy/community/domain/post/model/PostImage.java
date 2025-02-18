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

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private int imageOrder;

    public PostImage(String imageUrl, String s3Key, String fileType, int imageOrder) {
        this.imageUrl = imageUrl;
        this.s3Key = s3Key;
        this.fileType = fileType;
        this.imageOrder = imageOrder;
    }

    public static PostImage of(String imageUrl, String storedFileName, String fileType, int imageOrder) {
        return new PostImage(imageUrl, storedFileName, fileType, imageOrder);
    }

    public void assignPost(Post post) { this.post = post; }

    public void changeImageOrder(int newOrder) { this.imageOrder = newOrder; }
}
