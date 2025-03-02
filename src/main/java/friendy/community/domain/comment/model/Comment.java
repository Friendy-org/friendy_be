package friendy.community.domain.comment.model;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.common.BaseEntity;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.model.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer likeCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer replyCount;

    private Comment(final Member member, final Post post, final String content) {
        this.member = member;
        this.post = post;
        this.content = content;
        this.likeCount = 0;
        this.replyCount = 0;
    }

    public static Comment of(final CommentCreateRequest request, final Member member, final Post post) {
        return new Comment(member, post, request.content());
    }

    public void updateReplyCount(final Integer replyCount) {
        this.replyCount = replyCount;
    }

    public void updateLikeCount(final Integer likeCount) {
        this.likeCount = likeCount;
    }

    public void updateContent(final String content) {
        this.content = content;
    }
}
