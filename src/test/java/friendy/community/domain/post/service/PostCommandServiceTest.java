package friendy.community.domain.post.service;

import friendy.community.domain.hashtag.service.HashtagService;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberDomainService;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.PostIdResponse;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @InjectMocks
    private PostCommandService postCommandService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberCommandService memberCommandService;

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private PostDomainService postDomainService;

    @Mock
    private PostImageService postImageService;

    @Mock
    private HashtagService hashtagService;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = MemberFixture.memberFixture();
        ReflectionTestUtils.setField(member, "id", 1L);

        post = PostFixture.postFixture();
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(post, "member", member);
    }

    @Test
    @DisplayName("게시글 저장 성공")
    void savePost_success() {
        // given
        PostCreateRequest request = new PostCreateRequest(
            "content",
            List.of("tag1", "tag2"),
            List.of("url1", "url2"),
            "부산"
        );

        when(memberDomainService.getMemberById(1L)).thenReturn(member);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", 1L);
            return savedPost;
        });

        // when
        long savedId = postCommandService.savePost(request, 1L);

        // then
        assertThat(savedId).isEqualTo(1L);
        verify(postImageService).saveImagesForPost(any(Post.class), eq(request.imageUrls()));
        verify(hashtagService).saveHashtags(any(Post.class), eq(request.hashtags()));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_success() {
        // given
        PostUpdateRequest request = new PostUpdateRequest(
            "updated",
            List.of("newTag"),
            List.of("url"),
            "서울시"
        );

        when(memberDomainService.getMemberById(1L)).thenReturn(member);
        when(postDomainService.validatePostExistence(1L)).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", 1L);
            return savedPost;
        });

        // when
        PostIdResponse response = postCommandService.updatePost(request, 1L, 1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(postImageService).updateImagesForPost(post, request.imageUrls());
        verify(hashtagService).updateHashtags(post, request.hashtags());
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_success() {
        // given
        when(memberDomainService.getMemberById(1L)).thenReturn(member);
        when(postDomainService.validatePostExistence(1L)).thenReturn(post);

        // when
        postCommandService.deletePost(1L, 1L);

        // then
        verify(postDomainService).validatePostAuthor(member, post);
        verify(postImageService).deleteImagesForPost(post);
        verify(hashtagService).deleteHashtags(1L);
        verify(postRepository, times(1)).delete(post);
    }
}
