package friendy.community.domain.follow.service;

import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowCommandServiceTest {

    @Mock
    private MemberDomainService memberDomainService;

    @Mock
    private FollowDomainService followDomainService;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowCommandService followCommandService;

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
    @DisplayName("팔로우 성공 시 Follow가 저장되어야 한다")
    void follow_shouldSaveFollow_whenValid() {
        // given
        when(memberDomainService.getMemberById(1L)).thenReturn(follower);
        when(memberDomainService.getMemberById(2L)).thenReturn(following);

        // when
        followCommandService.follow(1L, 2L);

        // then
        verify(followDomainService).validateFollowable(follower, following);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("언팔로우 성공 시 Follow가 삭제되어야 한다")
    void unfollow_shouldDeleteFollow_whenValid() {
        // given
        when(memberDomainService.getMemberById(1L)).thenReturn(follower);
        when(memberDomainService.getMemberById(2L)).thenReturn(following);
        when(followRepository.findByFollowerAndFollowing(follower, following))
            .thenReturn(Follow.of(follower, following));

        // when
        followCommandService.unfollow(1L, 2L);

        // then
        verify(followDomainService).validateUnfollowable(follower, following);
        verify(followRepository).delete(any(Follow.class));
    }
}
