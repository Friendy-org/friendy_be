package friendy.community.domain.post.service;

import friendy.community.domain.member.dto.response.FindMemberPostsResponse;
import friendy.community.domain.post.controller.code.PostExceptionCode;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.domain.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @InjectMocks
    private PostQueryService postQueryService;

    @Mock
    private PostRepository postRepository;

    private Post post;

    @BeforeEach
    void setUp() {
        post = PostFixture.postFixture();
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(post.getMember(), "id", 1L);
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getPost_success() {
        // given
        when(postRepository.findPostById(1L)).thenReturn(Optional.of(post));

        // when
        FindPostResponse response = postQueryService.getPost(1L, 1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.me()).isTrue();
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 존재하지 않음")
    void getPost_notFound() {
        // given
        when(postRepository.findPostById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postQueryService.getPost(1L, 1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(PostExceptionCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 페이지 조회 성공 - lastPostId == null")
    void getPostsByLastId_success_nullLastId() {
        // given
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Post p = PostFixture.postFixture();
            ReflectionTestUtils.setField(p, "id", (long) (i + 1));
            ReflectionTestUtils.setField(p.getMember(), "id", 1L);
            posts.add(p);
        }
        when(postRepository.findPostsByLastId(null, 10)).thenReturn(posts);

        // when
        FindAllPostResponse response = postQueryService.getPostsByLastId(null, 1L);

        // then
        assertThat(response.posts()).hasSize(10);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.lastPostId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("게시글 페이지 조회 성공 - lastPostId != null")
    void getPostsByLastId_success_notNullLastId() {
        // given
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Post p = PostFixture.postFixture();
            ReflectionTestUtils.setField(p, "id", (long) (100 + i));
            ReflectionTestUtils.setField(p.getMember(), "id", 2L);
            posts.add(p);
        }
        when(postRepository.findPostsByLastId(100L, 10)).thenReturn(posts);

        // when
        FindAllPostResponse response = postQueryService.getPostsByLastId(100L, 2L);

        // then
        assertThat(response.posts()).hasSize(10);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.lastPostId()).isEqualTo(109L);
    }

    @Test
    @DisplayName("게시글 페이지 조회 실패 - 비어있음")
    void getPostsByLastId_notFound() {
        // given
        when(postRepository.findPostsByLastId(null, 10)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> postQueryService.getPostsByLastId(null, 1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(PostExceptionCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 단건 엔티티 조회 성공")
    void getPostById_success() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        Post found = postQueryService.getPostById(1L);

        // then
        assertThat(found).isEqualTo(post);
    }

    @Test
    @DisplayName("게시글 단건 엔티티 조회 실패 - 존재하지 않음")
    void getPostById_notFound() {
        // given
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postQueryService.getPostById(1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(PostExceptionCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원의 게시글 목록 조회 성공 - lastPostId null")
    void getMemberPosts_success_lastPostIdNull() {
        // given
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Post p = PostFixture.postFixture();
            ReflectionTestUtils.setField(p, "id", (long) (i + 1));
            posts.add(p);
        }
        when(postRepository.findPostsByMemberId(1L, null)).thenReturn(posts);

        // when
        FindMemberPostsResponse response = postQueryService.getMemberPosts(1L, null);

        // then
        assertThat(response.posts()).hasSize(2);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("회원의 게시글 목록 조회 성공 - lastPostId not null")
    void getMemberPosts_success_lastPostIdNotNull() {
        // given
        List<Post> posts = new ArrayList<>();
        for (int i = 0; i < 13; i++) {
            Post p = PostFixture.postFixture();
            ReflectionTestUtils.setField(p, "id", (long) (200 + i));
            posts.add(p);
        }
        when(postRepository.findPostsByMemberId(1L, 200L)).thenReturn(posts);

        // when
        FindMemberPostsResponse response = postQueryService.getMemberPosts(1L, 200L);

        // then
        assertThat(response.posts()).hasSize(12);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.lastPostId()).isEqualTo(211L);
    }
}
