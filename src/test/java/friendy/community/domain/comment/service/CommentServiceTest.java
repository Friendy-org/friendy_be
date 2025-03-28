package friendy.community.domain.comment.service;

import friendy.community.domain.comment.controller.code.CommentExceptionCode;
import friendy.community.domain.comment.dto.FindAllReplyResponse;
import friendy.community.domain.comment.dto.request.CommentCreateRequest;
import friendy.community.domain.comment.dto.request.CommentUpdateRequest;
import friendy.community.domain.comment.dto.request.ReplyCreateRequest;
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
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.domain.post.service.PostService;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.global.exception.domain.UnAuthorizedException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private EntityManager entityManager;

    private Member member;

    @BeforeEach
    void setUp() {
        resetDataBase();

        member = MemberFixture.memberFixture();
        Long memberId = memberService.signup(new MemberSignUpRequest(member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate(), null));

        member = memberService.findMemberById(memberId);

        Post post = PostFixture.postFixture();
        postService.savePost(new PostCreateRequest(post.getContent(), List.of("프렌디", "개발", "스터디"), null), member.getId());
    }

    private void resetDataBase() {
        commentRepository.deleteAll();
        replyRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE member AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE post AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE comment AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reply AUTO_INCREMENT = 1;").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    private void createComment() {
        commentService.saveComment(new CommentCreateRequest("new Valid Comment", 1L), member.getId());
    }

    @Test
    @DisplayName("댓글 생성에 성공하면 데이터베이스에 댓글이 저장된다.")
    void createCommentSuccessfullyReturnsCommentId() {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid comment contents", 1L);

        // When
        commentService.saveComment(request, member.getId());

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

        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("new valid reply contents", 1L, 1L);

        // When
        commentService.saveReply(request, member.getId());

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
        assertThatThrownBy(() -> commentService.saveComment(request, member.getId()))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("답글 작성 대상 댓글이 존재하지 않는 경우 404 Not Found 예외를 발생한다.")
    void createReplyWithNonExistCommentThrows404NotFound() {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("contents with non-exist comment", 1L, 2025L);

        // When & Then
        assertThatThrownBy(() -> commentService.saveReply(request, member.getId()))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정에 성공하면 저장된 댓글의 내용이 바뀐다.")
    void updateCommentSuccessfullyChangesSavedCommentsContent() {
        // Given
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");
        createComment();

        // When
        commentService.updateComment(commentUpdateRequest, 1L, member.getId());

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
        assertThatThrownBy(() -> commentService.updateComment(commentUpdateRequest, 2025L, member.getId()))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.COMMENT_NOT_FOUND);  // 예외 코드 확인
    }

    @Test
    @DisplayName("다른 사람의 댓글을 수정 요청하면 401 UNAUTHORIZED 예외를 발생한다.")
    void updateOtherUsersCommentThrows401Unauthorized() {
        // Given
        createComment();
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");

        memberService.signup(new MemberSignUpRequest(
            "user@example.com", "홍길동", "password123!", LocalDate.parse("2002-08-13"), null));
        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(commentUpdateRequest, 1L, 2L))
            .isInstanceOf(UnAuthorizedException.class)
            .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.UNAUTHORIZED_COMMENT_USER);
    }

    @Test
    @DisplayName("답글 수정에 성공하면 저장된 답글의 내용이 바뀐다.")
    void updateReplySuccessfullyChangesSavedReplyContent() {
        // Given
        createComment();
        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest("origin valid content", 1L, 1L);
        commentService.saveReply(replyCreateRequest, member.getId());

        CommentUpdateRequest updateRequest = new CommentUpdateRequest("new valid content");

        // When
        commentService.updateReply(updateRequest, 1L, member.getId());

        // Then
        List<Reply> savedReplies = replyRepository.findAll();
        assertThat(savedReplies.size()).isEqualTo(1);
        assertThat(savedReplies).extracting(Reply::getContent).contains("new valid content");
    }

    @Test
    @DisplayName("존재하지 않는 답글 id를 수정 요청하면 404 Not Found 예외를 발생한다.")
    void updateReplyWithNonExistsReplyThrows404NotFound() {
        // Given
        createComment();
        CommentUpdateRequest updateRequest = new CommentUpdateRequest("updated valid content");

        // When & Then
        assertThatThrownBy(() -> commentService.updateReply(updateRequest, 2025L, member.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사용자의 답글을 수정 요청하면 401 Unauthorized 예외를 발생한다.")
    void updateOtherUsersReplyThrows401Unauthorized() {
        // Given
        createComment();

        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest("origin valid content", 1L, 1L);
        commentService.saveReply(replyCreateRequest, member.getId());

        Reply savedReply = replyRepository.findAll().getFirst();

        memberService.signup(new MemberSignUpRequest(
            "user@example.com", "홍길동", "password123!", LocalDate.parse("2002-08-13"), null));

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("new valid content");

        // When & Then
        assertThatThrownBy(() -> commentService.updateReply(commentUpdateRequest, savedReply.getId(), 2L))
            .isInstanceOf(UnAuthorizedException.class)
            .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.UNAUTHORIZED_REPLY_USER);
    }

    @Test
    @DisplayName("댓글 삭제에 성공하면 부모 게시글의 댓글 개수가 1 감소한다.")
    void deleteCommentSuccessfullyDecreasesCommentCountOfParentPost() {
        // Given
        createComment();

        // When
        commentService.deleteComment(1L, member.getId());

        // Then
        List<Comment> comments = commentRepository.findAll();
        List<Post> posts = postRepository.findAll();
        assertThat(comments.size()).isEqualTo(0);
        assertThat(posts.getFirst().getCommentCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("답글이 달려있는 댓글을 삭제하면 해당 댓글과 답글이 모두 삭제된다.")
    void deleteCommentWithRepliesSuccessfullyDeletesAllOfThem() {
        // Given
        createComment();
        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest("new valid reply", 1L, 1L);
        commentService.saveReply(replyCreateRequest, member.getId());
        Comment savedComment = commentRepository.findAll().getFirst();
        Reply savedReply = replyRepository.findAll().getFirst();

        // When
        commentService.deleteComment(savedComment.getId(), member.getId());

        // Then
        List<Comment> comments = commentRepository.findAll();
        List<Reply> replies = replyRepository.findAll();
        List<Post> posts = postRepository.findAll();
        assertThat(comments.size()).isEqualTo(0);
        assertThat(replies.size()).isEqualTo(0);
        assertThat(posts.getFirst().getCommentCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("답글 삭제에 성공하면 부모 댓글의 답글 수가 1 감소한다.")
    void deleteReplySuccessfullyDecreasesReplyCountOfParentComment() {
        // When
        createComment();
        ReplyCreateRequest replyCreateRequest = new ReplyCreateRequest("new valid reply", 1L, 1L);
        commentService.saveReply(replyCreateRequest, member.getId());
        Reply savedReply = replyRepository.findAll().getFirst();

        // When
        commentService.deleteReply(savedReply.getId(), member.getId());

        // Then
        List<Comment> comments = commentRepository.findAll();
        List<Reply> replies = replyRepository.findAll();
        assertThat(replies.size()).isEqualTo(0);
        assertThat(comments.getFirst().getReplyCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("삭제할 답글이 존재하지 않으면 오류가 발생한다.")
    void deleteNonExistReplyThrows404NotFound() {
        // When & Then
        assertThatThrownBy(() -> commentService.deleteReply(1L, 1L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.REPLY_NOT_FOUND);
    }

    @Test
    @DisplayName("답글 목록 조회 성공")
    void getAllRepliesSuccessfullyReturnsFindAllReplyResponse() {
        // Given
        createComment();
        for (int i = 0; i < 15; i++) {
            commentService.saveReply(new ReplyCreateRequest("new valid content", 1L, 1L), 1L);
        }

        // When
        FindAllReplyResponse firstResponse = commentService.getRepliesByLastId(null);
        FindAllReplyResponse secondResponse = commentService.getRepliesByLastId(firstResponse.lastPostId());

        assertThat(firstResponse.replies().size()).isEqualTo(10);
        assertThat(secondResponse.replies().size()).isEqualTo(5);
    }

    @Test
    @DisplayName("답글 목록 조회 시 답글이 없으면 예외가 발생한다.")
    void getRepliesByLastIdThrowsExceptionWhenNoReplies() {
        // When & Then
        assertThatThrownBy(() -> commentService.getRepliesByLastId(null))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.REPLY_NOT_FOUND);

        assertThatThrownBy(() -> commentService.getRepliesByLastId(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("exceptionType", CommentExceptionCode.REPLY_NOT_FOUND);
    }
}
