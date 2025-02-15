package friendy.community.domain.comment.fixture;

import friendy.community.domain.comment.CommentType;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;

public class CommentFixture {
    public static Comment commentFixture() {
        return createComment("new Valid Comment", 1L);
    }

    private static Comment createComment(String content, Long postId) {
        Member member = MemberFixture.memberFixture();
        Post post = PostFixture.postFixture();
        return Comment.of(new CommentCreateRequest(content, postId, CommentType.COMMENT), member, post);
    }
}
