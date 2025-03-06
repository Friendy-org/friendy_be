package friendy.community.domain.comment.service;

import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.model.Reply;
import friendy.community.domain.comment.repository.CommentRepository;
import friendy.community.domain.comment.repository.ReplyRepository;
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

import java.time.LocalDate;
import java.util.List;

import static friendy.community.domain.auth.fixtures.TokenFixtures.OTHER_USER_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static friendy.community.domain.auth.fixtures.TokenFixtures.CORRECT_ACCESS_TOKEN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@SpringBootTest
@Transactional
@DirtiesContext
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AuthService authService;
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
                member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate(), null));

        authService.login(new LoginRequest(member.getEmail(), member.getPassword()));

        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Authorization", CORRECT_ACCESS_TOKEN);

        Post post = PostFixture.postFixture();
        postService.savePost(new PostCreateRequest(post.getContent(), List.of("프렌디", "개발", "스터디"), null), httpServletRequest);
    }

    private void resetDataBase() {
        commentRepository.deleteAll();
        replyRepository.deleteAll();
        entityManager.createNativeQuery("TRUNCATE TABLE post AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE comment AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    private void createComment() {
        commentService.saveComment(new CommentCreateRequest("new Valid Comment", 1L), httpServletRequest);
    }

    @Test
    @DisplayName("댓글 생성에 성공하면 데이터베이스에 댓글이 저장된다.")
    void createCommentSuccessfullyReturnsCommentId() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid comment contents", 1L);

        // When
        commentService.saveComment(request, httpServletRequest);

        // Then
        List<Comment> savedComment = commentRepository.findAll();
        assertThat(savedComment.size()).isEqualTo(1);
        assertThat(savedComment).extracting(Comment::getContent).contains("new valid comment contents");
    }

    @Test
    @DisplayName("답글 생성에 성공하면 데이터베이스에 답글이 저장된다.")
    void createReplySuccesfullyReturnsReplyId() {
        // Given
        createComment();
        ReplyCreateRequest request = new ReplyCreateRequest("new valid reply contents", 1L, 1L);

        // When
        commentService.saveReply(request, httpServletRequest);

        // Then
        List<Reply> savedReplies = replyRepository.findAll();
        assertThat(savedReplies.size()).isEqualTo(1);
        assertThat(savedReplies).extracting(Reply::getContent).contains("new valid reply contents");

        List<Comment> savedComments = commentRepository.findAll();
        assertThat(savedComments.getFirst()).extracting("replyCount").isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 작성 대상 게시글이 존재하지 않는 경우 404 Not Found 예외를 발생한다.")
    void createCommentWithNonExistPostThrows404NotFound() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("contents with non-exist post", 2025L);

        // When & Then
        assertThatThrownBy(() -> commentService.saveComment(request, httpServletRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("댓글 작성 대상 게시글이 존재하지 않습니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("답글 작성 대상 댓글이 존재하지 않는 경우 404 Not Found 예외를 발생한다.")
    void createReplyWithNonExistCommentThrows404NotFound() {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("contents with non-exist comment", 1L, 2025L);

        // When & Then
        assertThatThrownBy(() -> commentService.saveReply(request, httpServletRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("존재하지 않는 댓글입니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("댓글 수정에 성공하면 저장된 댓글의 내용이 바뀐다.")
    void updateCommentSuccessfullyChangesSavedCommentsContent() {
        // Given
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");
        createComment();

        // When
        commentService.updateComment(commentUpdateRequest, 1L, httpServletRequest);

        // Then
        List<Comment> savedComments = commentRepository.findAll();
        assertThat(savedComments.size()).isEqualTo(1);
        assertThat(savedComments).extracting(Comment::getContent).contains("new valid content");
    }

    @Test
    @DisplayName("존재하지 않는 댓글 id를 수정 요청하면 404 Not Found 예외를 발생한다.")
    void updateCommentWithNonExistCommentThrows404NotFound() {
        // Given
        createComment();
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");

        // When & Then
        List<Comment> savedComments = commentRepository.findAll();
        assertThatThrownBy(() -> commentService.updateComment(commentUpdateRequest, 2025L, httpServletRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("존재하지 않는 댓글입니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사람의 댓글을 수정 요청하면 401 UNAUTHORIZED 예외를 발생한다.")
    void updateOtherUsersCommentThrows401Unauthorized() {
        // Given
        createComment();
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");

        memberService.signUp(new MemberSignUpRequest(
                "user@example.com", "홍길동", "password123!", LocalDate.parse("2002-08-13"),null));
        authService.login(new LoginRequest("user@example.com", "password123!"));
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Authorization", OTHER_USER_TOKEN);

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(commentUpdateRequest, 1L, httpServletRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("작성자만 댓글을 수정할 수 있습니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_USER);
    }

    @Test
    @DisplayName("답글 수정에 성공하면 저장된 답글의 내용이 바뀐다.")
    void updateReplySuccessfullyChangesSavedReplyContent() {
        // Given
        createComment();
        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest("origin valid content", 1L, 1L);
        commentService.saveReply(replyCreateRequest, httpServletRequest);

        CommentUpdateRequest updateRequest = new CommentUpdateRequest("new valid content");

        // When
        commentService.updateReply(updateRequest, 1L,  httpServletRequest);

        // Then
        List<Reply> savedReplies = replyRepository.findAll();
        assertThat(savedReplies.size()).isEqualTo(1);
        assertThat(savedReplies).extracting(Reply::getContent).contains("new valid content");
    }

    @Test
    @DisplayName("다른 사용자의 답글을 수정 요청하면 401 Unauthorized 예외를 발생한다.")
    void updateOtherUsersReplyThrows401Unauthorized() {
        // Given
        createComment();

        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest("origin valid content", 1L, 1L);
        commentService.saveReply(replyCreateRequest, httpServletRequest);

        Reply savedReply = replyRepository.findAll().getFirst();

        memberService.signUp(new MemberSignUpRequest(
                "user@example.com", "홍길동", "password123!", LocalDate.parse("2002-08-13"), null));
        authService.login(new LoginRequest("user@example.com", "password123!"));
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Authorization", OTHER_USER_TOKEN);

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");

        // When & Then
        assertThatThrownBy(() -> commentService.updateReply(commentUpdateRequest, savedReply.getId(), httpServletRequest))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("작성자만 답글을 수정할 수 있습니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_USER);
    }
}
