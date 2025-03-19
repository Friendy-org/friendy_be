package friendy.community.domain.post.model;

import friendy.community.domain.common.BaseEntity;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer likeCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer commentCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer shareCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    protected Post(final PostCreateRequest request, final Member member) {
        this.member = member;
        this.content = request.content();
        this.likeCount = 0;
        this.commentCount = 0;
        this.shareCount = 0;
    }

    public void addImage(PostImage image) {
        images.add(image);
        image.assignPost(this);
    }

    public static Post of(final PostCreateRequest request, final Member member) {
        return new Post(request, member);
    }

    public void updatePost(final PostUpdateRequest postUpdateRequest) {
        this.content = postUpdateRequest.content();
    }

    public void updateCommentCount(final Integer commentCount) {
        this.commentCount = commentCount;
    }
}