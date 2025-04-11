package friendy.community.domain.post.service;

import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.post.controller.code.PostExceptionCode;
import friendy.community.domain.post.fixture.PostFixture;
import friendy.community.domain.post.model.Post;
import friendy.community.domain.post.repository.PostRepository;
import friendy.community.global.exception.domain.NotFoundException;
import friendy.community.global.exception.domain.UnAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostDomainServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostDomainService postDomainService;

    private Member member;
    private Post post;

    @BeforeEach
    void setUp() {
        member = MemberFixture.memberFixture();
        post = PostFixture.postFixture();
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(post.getMember(), "id", 1L);
    }

    @Test
    @DisplayName("게시글 존재 유효성 검사 - 실패 (예외 발생)")
    void validatePostExistence_notFound() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postDomainService.validatePostExistence(1L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage(PostExceptionCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("게시글 작성자 유효성 검사 - 실패 (예외 발생)")
    void validatePostAuthor_unauthorized() {
        // given
        Member another = MemberFixture.memberFixture();
        ReflectionTestUtils.setField(another, "id", 999L);

        // when & then
        assertThatThrownBy(() -> postDomainService.validatePostAuthor(another, post))
            .isInstanceOf(UnAuthorizedException.class)
            .hasMessage(PostExceptionCode.POST_FORBIDDEN_ACCESS.getMessage());
    }

    @Test
    @DisplayName("게시글 작성자 유효성 검사 - 성공")
    void validatePostAuthor_success() {
        // when & then
        postDomainService.validatePostAuthor(member, post);
    }

    @Test
    @DisplayName("게시글 존재 유효성 검사 - 성공")
    void validatePostExistence_success() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        Post found = postDomainService.validatePostExistence(1L);

        // then
        assertThat(found).isEqualTo(post);
    }
}
