package friendy.community.domain.comment.model;

import friendy.community.domain.comment.CommentType;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.fixture.CommentFixture;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CommentTest {

    private Member member;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setComment() {
        member = MemberFixture.memberFixture();
        post = PostFixture.postFixture();
        comment = CommentFixture.commentFixture();
    }

    @Test
    @DisplayName("Comment 객체가 댓글 생성 요청을 기반으로 생성된다.")
    void ofMethodCreatesCommentFromCommentRequest() {
        // Given
        String content = "This is a new comment content.";
        CommentType type = CommentType.COMMENT;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(content, post.getId(), type);

        // When
        Comment comment = Comment.of(commentCreateRequest, member, post);

        // Then
        assertNotNull(comment);
        assertNull(comment.getParentComment());
        assertEquals(content, comment.getContent());
        assertEquals(member, comment.getMember());
        assertEquals(post, comment.getPost());
        assertEquals(type, comment.getType());
    }

    @Test
    @DisplayName("Comment 객체가 답글 생성 요청을 기반으로 생성된다.")
    void ofMethodCreatesCommentFromReplyRequest() {
        // Given
        String content = "This is a new reply content.";
        CommentType type = CommentType.REPLY;
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(content, post.getId(), type);

        // When
        Comment reply = Comment.of(commentCreateRequest, member, post, comment);

        // Then
        assertNotNull(reply);
        assertEquals(content, reply.getContent());
        assertEquals(comment, reply.getParentComment());
        assertEquals(member, reply.getMember());
        assertEquals(post, reply.getPost());
        assertEquals(type, reply.getType());
    }

}
