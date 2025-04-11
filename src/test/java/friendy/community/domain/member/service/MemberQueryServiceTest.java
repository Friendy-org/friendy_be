package friendy.community.domain.member.service;

import friendy.community.domain.follow.service.FollowQueryService;
import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.response.FindMemberPostsResponse;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.dto.response.PostPreview;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.model.MemberImage;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.post.service.PostQueryService;
import friendy.community.global.exception.domain.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @Mock
    PostQueryService postQueryService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FollowQueryService followQueryService;

    @InjectMocks
    private MemberQueryService memberQueryService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.of(
            new MemberSignUpRequest("test@example.com", "nickname", "password123!", LocalDate.of(2000, 1, 1), "test.jpg"),
            "encrypted-password",
            "salt123"
        );
        member.updateMemberImage(new MemberImage("https://example.com/image.jpg", "key", "jpg"));
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Test
    @DisplayName("회원 정보를 조회한다")
    void shouldGetMemberInfo() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(followQueryService.getFollowerCount(member)).thenReturn(10);
        when(followQueryService.getFollowingCount(member)).thenReturn(5);

        // when
        FindMemberResponse response = memberQueryService.getMemberInfo(1L, 1L);

        // then
        assertThat(response.me()).isTrue();
        assertThat(response.followerCount()).isEqualTo(10);
        assertThat(response.followingCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 정보를 조회할 경우 NotFoundException이 발생한다")
    void shouldThrowExceptionWhenMemberNotFound() {
        // given
        Long nonexistentMemberId = 999L;
        Long requesterId = 1L;
        when(memberRepository.findById(nonexistentMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberQueryService.getMemberInfo(requesterId, nonexistentMemberId))
            .isInstanceOf(NotFoundException.class)
            .satisfies(e -> assertThat(((NotFoundException) e).getExceptionType())
                .isEqualTo(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));
    }

    @Test
    @DisplayName("회원의 게시글 목록을 조회한다")
    void shouldReturnMemberPosts() {
        // given
        Long memberId = 1L;
        Long lastPostId = null;

        List<PostPreview> mockPosts = List.of(
            new PostPreview(1L, "thumbnail1"),
            new PostPreview(2L, "thumbnail2")
        );
        FindMemberPostsResponse response = new FindMemberPostsResponse(mockPosts, true, 2L);

        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(postQueryService.getMemberPosts(memberId, lastPostId)).thenReturn(response);

        // when
        FindMemberPostsResponse result = memberQueryService.getMemberPosts(memberId, lastPostId);

        // then
        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 게시글을 조회할 경우 NotFoundException이 발생한다")
    void shouldThrowNotFoundExceptionWhenMemberNotExists() {
        // given
        Long memberId = 999L;
        when(memberRepository.existsById(memberId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> memberQueryService.getMemberPosts(memberId, null))
            .isInstanceOf(NotFoundException.class)
            .satisfies(e -> assertThat(((NotFoundException) e).getExceptionType())
                .isEqualTo(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));
    }
}
