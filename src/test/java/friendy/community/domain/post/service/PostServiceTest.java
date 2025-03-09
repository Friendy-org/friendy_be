package friendy.community.domain.post.service;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.model.Comment;
import friendy.community.domain.comment.repository.CommentRepository;
import friendy.community.domain.comment.service.CommentService;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.member.service.MemberService;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import friendy.community.infra.storage.s3.service.S3service;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@DirtiesContext
class PostServiceTest {

    @Autowired
    private PostService postService;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EntityManager entityManager;
    @MockitoBean
    private S3service s3Service;

    private Member member;

    @BeforeEach
    void setup() {
        resetPostIdSequence();
        resetMemberIdSequence();
        setupS3ServiceMock();

        member = MemberFixture.memberFixture();

        Long memberId = memberService.signUp(new MemberSignUpRequest(
            member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate(), null));

        member = memberService.findMemberById(memberId);
    }

    private void setupS3ServiceMock() {
        String mockedImageUrl = "https://s3.us-east-1.amazonaws.com/post/image.jpg";
        String mockedS3Key = "post/image.jpg";
        String mockedFileType = "image/jpeg";

        when(s3Service.moveS3Object(anyString(), eq("post")))
            .thenReturn(mockedImageUrl)
            .thenReturn("https://example.com/test.jpg");
        when(s3Service.extractFilePath(anyString())).thenReturn(mockedS3Key);
        when(s3Service.getContentTypeFromS3(anyString())).thenReturn(mockedFileType);
    }

    private void resetPostIdSequence() {
        entityManager.createNativeQuery("ALTER TABLE post AUTO_INCREMENT = 1").executeUpdate();
    }

    private void resetMemberIdSequence() {
        entityManager.createNativeQuery("ALTER TABLE member AUTO_INCREMENT = 1").executeUpdate();
    }

    private Long createPost() {
        Post post = PostFixture.postFixture();
        return postService.savePost(new PostCreateRequest(post.getContent(), List.of("프렌디", "개발", "스터디"), null), member.getId());
    }

    private void signUpOtherUser() {
        memberService.signUp(new MemberSignUpRequest(
            "user@example.com", "홍길동", "password123!", LocalDate.parse("2002-08-13"), null));
    }

    @Test
    @DisplayName("게시글 생성 성공 시 게시글 ID 반환")
    void createPostSuccessfullyReturnsPostId() {
        // Given & When
        Long postId = createPost();

        // Then
        assertThat(postId).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 수정 성공 시 게시글 ID 반환")
    void updatePostSuccessfullyReturnsPostId() {
        // Given
        createPost();
        PostUpdateRequest request = new PostUpdateRequest("Updated content", List.of("업데이트"), null);

        // When
        Long postId = postService.updatePost(request, member.getId(), 1L);
        Post updatedPost = postRepository.findById(1L)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 게시글입니다."));

        // Then
        assertThat(postId).isEqualTo(1L);
        assertThat(updatedPost.getContent()).isEqualTo("Updated content");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
    void throwsExceptionWhenPostNotFoundOnUpdate() {
        // Given
        PostUpdateRequest request = new PostUpdateRequest("Updated content", List.of("업데이트"), null);

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(request, member.getId(), 999L))
            .isInstanceOf(FriendyException.class)
            .hasMessageContaining("존재하지 않는 게시글입니다.")
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 작성자가 아닌 사용자가 수정 시 예외 발생")
    void throwsExceptionWhenNotPostAuthorOnUpdate() {
        // Given
        createPost();

        // When
        signUpOtherUser();

        // Then
        assertThatThrownBy(() -> postService.updatePost(
            new PostUpdateRequest("Updated content", List.of("업데이트"), null), 2L, 1L), null)
            .isInstanceOf(FriendyException.class)
            .hasMessageContaining("게시글은 작성자 본인만 관리할 수 있습니다.")
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePostSuccessfullyDeletesPost() {
        // Given
        postService.savePost(new PostCreateRequest("content",
            List.of("프렌디", "개발", "스터디"),
            List.of("https://example.com/image1.jpg")), member.getId());

        doNothing().when(s3Service).deleteFromS3(anyString());
        // When
        postService.deletePost(member.getId(), 1L);

        // Then
        assertThat(postRepository.existsById(1L)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
    void throwsExceptionWhenPostNotFoundOnDelete() {
        // When & Then
        assertThatThrownBy(() -> postService.deletePost(member.getId(), 999L))
            .isInstanceOf(FriendyException.class)
            .hasMessageContaining("존재하지 않는 게시글입니다.")
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 작성자가 아닌 사용자가 삭제 시 예외 발생")
    void throwsExceptionWhenNotPostAuthorOnDelete() {
        // Given
        createPost();

        // When
        signUpOtherUser();

        // Then
        assertThatThrownBy(() -> postService.deletePost(2L, 1L))
            .isInstanceOf(FriendyException.class)
            .hasMessageContaining("게시글은 작성자 본인만 관리할 수 있습니다.")
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
    }

    @Test
    @DisplayName("댓글이 달린 게시글 삭제 시 댓글도 모두 삭제된다.")
    void deletePostWithCommentsSuccessfullyDeletesAllOfThen() {
        // Given
        createPost();
        Post post = postRepository.findAll().getFirst();

        CommentCreateRequest commentRequest = new CommentCreateRequest("new valid comment", post.getId());
        commentService.saveComment(commentRequest, httpServletRequest);

        // When
        postService.deletePost(httpServletRequest, post.getId());

        // Then
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments.size()).isEqualTo(0);
        assertThat(post.getCommentCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글 조회 요청이 성공적으로 수행되면 FindPostResponse를 리턴한다")
    void getPostSuccessfullyReturnsFindPostResponse() {
        // Given
        createPost();

        // When
        FindPostResponse response = postService.getPost(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("This is a sample post content.");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
    void getPostWithNonExistentIdThrowsException() {
        // When & Then
        assertThatThrownBy(() -> postService.getPost(999L))
            .isInstanceOf(FriendyException.class)
            .hasMessageContaining("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getAllPostsSuccessfullyReturnsFindAllPostResponse() {
        // Given
        createPost();
        createPost();

        // When
        FindAllPostResponse response = postService.getAllPosts(PageRequest.of(0, 10));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.posts()).extracting("content")
            .containsExactlyInAnyOrder("This is a sample post content.", "This is a sample post content.");
    }

    @Test
    @DisplayName("존재하지 않는 페이지 요청 시 예외 발생")
    void requestingNonExistentPageThrowsException() {
        // When & Then
        assertThatThrownBy(() -> postService.getAllPosts(PageRequest.of(10, 10)))
            .isInstanceOf(FriendyException.class)
            .hasMessageContaining("요청한 페이지가 존재하지 않습니다.")
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 생성 성공 시 게시글 ID 반환(이미지 포함)")
    void createPostWithImageReturnsPostId() {
        // Given
        PostCreateRequest request = new PostCreateRequest(
            "프렌디 게시글 내용입니다.",
            List.of("프렌디", "개발", "스터디"),
            List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg")
        );

        // When
        Long postId = postService.savePost(request, member.getId());

        // Then
        assertThat(postId).isEqualTo(1L);
    }

    @Test
    @DisplayName("게시글 수정 성공 시 게시글 ID 반환(이미지포함)")
    void updatePostWithImageReturnsPostId() {
        // Given
        postService.savePost(new PostCreateRequest("content",
            List.of("프렌디", "개발", "스터디"),
            List.of("https://example.com/image1.jpg", "https://example.com/image2.jpg")), member.getId());

        PostUpdateRequest request = new PostUpdateRequest("Updated content", List.of("업데이트"),
            List.of("https://example.com/image1.jpg", "https://example.com/image3.jpg", "https://s3.us-east-1.amazonaws.com/post/image.jpg"));

        // When
        Long postId = postService.updatePost(request, member.getId(), 1L);
        Post updatedPost = postRepository.findById(1L)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 게시글입니다."));

        // Then
        assertThat(postId).isEqualTo(1L);
        assertThat(updatedPost.getContent()).isEqualTo("Updated content");
    }
}
