package friendy.community.domain.comment.model;

import friendy.community.domain.comment.CommentType;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentId", nullable = true)
    private Comment parentComment;

    @Column(nullable = false)
    private CommentType type;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer likeCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer replyCount;

    protected Comment(final CommentCreateRequest request, final Member member, final Post post) {
        this.member = member;
        this.post = post;
        this.content = request.content();
        this.type = request.type();
        this.likeCount = 0;
        this.replyCount = 0;
    }

    protected Comment(final CommentCreateRequest request, final Member member, final Post post, final Comment comment) {
        this(request, member, post);
        this.parentComment = comment;
    }

    public static Comment of(final CommentCreateRequest request, final Member member, final Post post) {
        return new Comment(request, member, post);
    }

    public static Comment of(final CommentCreateRequest request, final Member member, final Post post, final Comment comment) {
        return new Comment(request, member, post, comment);
    }

    public void updateComment(final CommentUpdateRequest request, final Member member) {
        this.content = request.content();
    }
}
