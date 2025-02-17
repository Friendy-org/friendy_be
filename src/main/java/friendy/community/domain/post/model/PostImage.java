package friendy.community.domain.post.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostImage {

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

    public PostImage(String imageUrl, String s3Key, String fileType) {
        this.imageUrl = imageUrl;
        this.s3Key = s3Key;
        this.fileType = fileType;
    }

    public static PostImage of(String imageUrl, String storedFileName, String fileType) {
        return new PostImage(imageUrl, storedFileName, fileType);
    }

    public void assignPost(Post post) {
        this.post = post;
    }

}
