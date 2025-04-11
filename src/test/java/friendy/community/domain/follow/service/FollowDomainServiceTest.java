package friendy.community.domain.follow.service;

import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowDomainServiceTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowDomainService followDomainService;

    private Member follower;
    private Member following;

    @BeforeEach
    void setup() {
        List<Member> members = MemberFixture.createMultipleMembers(2);
        follower = members.get(0);
        following = members.get(1);
        ReflectionTestUtils.setField(follower, "id", 1L);
        ReflectionTestUtils.setField(following, "id", 2L);
    }

    @Test
    @DisplayName("자기 자신을 팔로우하려 할 경우 예외가 발생해야 한다")
    void validateSelfFollow_shouldThrowException_whenSameMember() {
        // given
        ReflectionTestUtils.setField(following, "id", 1L);

        // when & then
        assertThatThrownBy(() -> followDomainService.validateSelfFollow(follower, following))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("이미 팔로우한 회원일 경우 예외가 발생해야 한다")
    void validateFollowable_shouldThrowException_whenAlreadyFollowed() {
        // given
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> followDomainService.validateFollowable(follower, following))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("팔로우가 가능한 경우 예외가 발생하지 않아야 한다")
    void validateFollowable_shouldPass_whenNotFollowedYet() {
        // given
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);

        // when & then
        assertThatCode(() -> followDomainService.validateFollowable(follower, following))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("팔로우하지 않은 회원을 언팔로우할 경우 예외가 발생해야 한다")
    void validateUnfollowable_shouldThrowException_whenNotFollowed() {
        // given
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> followDomainService.validateUnfollowable(follower, following))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("언팔로우가 가능한 경우 예외가 발생하지 않아야 한다")
    void validateUnfollowable_shouldPass_whenFollowed() {
        // given
        when(followRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);

        // when & then
        assertThatCode(() -> followDomainService.validateUnfollowable(follower, following))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("팔로잉 리스트가 비어있으면 예외가 발생해야 한다")
    void validateFollowingExists_shouldThrowException_whenEmpty() {
        // given
        List<Follow> follows = List.of();

        // when & then
        assertThatThrownBy(() -> followDomainService.validateFollowingExists(follows))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("팔로워 리스트가 비어있으면 예외가 발생해야 한다")
    void validateFollowerExists_shouldThrowException_whenEmpty() {
        // given
        List<Follow> follows = List.of();

        // when & then
        assertThatThrownBy(() -> followDomainService.validateFollowerExists(follows))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("팔로워 리스트가 존재하면 예외가 발생하지 않아야 한다")
    void validateFollowerExists_shouldPass_whenNotEmpty() {
        // given
        List<Follow> follows = List.of(Follow.of(follower, following));

        // when & then
        assertThatCode(() -> followDomainService.validateFollowerExists(follows))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("팔로잉 리스트가 존재하면 예외가 발생하지 않아야 한다")
    void validateFollowingExists_shouldPass_whenNotEmpty() {
        // given
        List<Follow> follows = List.of(Follow.of(follower, following));

        // when & then
        assertThatCode(() -> followDomainService.validateFollowingExists(follows))
            .doesNotThrowAnyException();
    }
}
