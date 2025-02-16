package friendy.community.domain.comment.service;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberService;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.service.PostService;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static friendy.community.domain.comment.CommentType.*;
import static friendy.community.domain.auth.fixtures.TokenFixtures.CORRECT_ACCESS_TOKEN;
import static org.mockito.ArgumentMatchers.isNotNull;

@SpringBootTest
@Transactional
@DirtiesContext
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PostService postService;
    @Autowired
    private EntityManager entityManager;

    private MockHttpServletRequest httpServletRequest;
    private Member member;

    @BeforeEach
    void setUp() {
        resetDataBase();

        member = MemberFixture.memberFixture();
        memberService.signUp(new MemberSignUpRequest(
                member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate()));

        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Authorization", CORRECT_ACCESS_TOKEN);

        Post post = PostFixture.postFixture();
        postService.savePost(new PostCreateRequest(post.getContent(), List.of("프렌디", "개발", "스터디")), httpServletRequest);
    }

    private void resetDataBase() {
        entityManager.createNativeQuery("ALTER TABLE post AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE comment AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("댓글 생성 성공 시 댓글 ID를 반환한다.")
    void createCommentSuccessfullyReturnsCommentId() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid contents", 1L, COMMENT);

        // When
        Long commentId = commentService.saveComment(request, httpServletRequest);

        // Then
        assertThat(commentId).isInstanceOf(Long.class);
        assertThat(commentId).isGreaterThan(0L);
    }

    @Test
    @DisplayName("답글 생성 성공 시 답글 ID를 반환한다.")
    void createReplySuccesfullyReturnsReplyId() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid contents", 1L, REPLY);

        // When
        Long replyId = commentService.saveComment(request, httpServletRequest);

        // Then
        assertThat(replyId).isInstanceOf(Long.class);
        assertThat(replyId).isGreaterThan(0L);
    }

    @Test
    @DisplayName("댓글 작성 대상 게시글이 존재하지 않는 경우 404 Not Found 예외를 발생한다.")
    void createCommentWithNonExistPostThrows404NotFound() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("contents with non-exist post", 2025L, COMMENT);

        // When & Then
        assertThatThrownBy(() -> commentService.saveComment(request, httpServletRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("댓글 작성 대상 게시글이 존재하지 않습니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }
}
