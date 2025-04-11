package friendy.community.domain.follow.service;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowQueryServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FollowDomainService followDomainService;

    @InjectMocks
    private FollowQueryService followQueryService;

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
    @DisplayName("팔로잉 리스트 조회 시 hasNext가 true여야 한다 (20개 초과)")
    void getFollowingList_shouldReturnFollowListResponse() {
        // given
        List<Member> members = MemberFixture.createMultipleMembers(30);
        List<Follow> follows = new ArrayList<>();
        for (Member following : members) {
            follows.add(Follow.of(follower, following));
        }
        when(followRepository.findFollowingFollows(anyLong(), any(), eq(20)))
            .thenReturn(follows);

        // when
        FollowListResponse response = followQueryService.getFollowingList(1L, null);

        // then
        verify(followDomainService).validateFollowingExists(follows);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("팔로워 리스트 조회 시 hasNext가 true여야 한다 (20개 초과)")
    void getFollowerList_shouldReturnFollowListResponse() {
        // given
        List<Member> members = MemberFixture.createMultipleMembers(30);
        List<Follow> follows = members.stream()
            .map(member -> Follow.of(member, following))
            .collect(Collectors.toList());
        when(followRepository.findFollowerFollows(anyLong(), any(), eq(20)))
            .thenReturn(follows);

        // when
        FollowListResponse response = followQueryService.getFollowerList(2L, null);

        // then
        verify(followDomainService).validateFollowerExists(follows);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    @DisplayName("팔로워 리스트 조회 시 hasNext가 false여야 한다 (20개 이하)")
    void getFollowerList_shouldReturnHasNextFalse_whenLessThanOrEqualTo20() {
        // given
        List<Member> members = MemberFixture.createMultipleMembers(15);
        List<Follow> follows = members.stream()
            .map(member -> Follow.of(member, following))
            .collect(Collectors.toList());
        when(followRepository.findFollowerFollows(anyLong(), any(), eq(20)))
            .thenReturn(follows);

        // when
        FollowListResponse response = followQueryService.getFollowerList(2L, null);

        // then
        verify(followDomainService).validateFollowerExists(follows);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.members()).hasSize(15);
    }

    @Test
    @DisplayName("팔로잉 리스트 조회 시 hasNext가 false여야 한다 (20개 이하)")
    void getFollowingList_shouldReturnHasNextFalse_whenLessThanOrEqualTo20() {
        // given
        List<Member> members = MemberFixture.createMultipleMembers(15);
        List<Follow> follows = members.stream()
            .map(following -> Follow.of(follower, following))
            .collect(Collectors.toList());
        when(followRepository.findFollowingFollows(anyLong(), any(), eq(20)))
            .thenReturn(follows);

        // when
        FollowListResponse response = followQueryService.getFollowingList(1L, null);

        // then
        verify(followDomainService).validateFollowingExists(follows);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.members()).hasSize(15);
    }

    @Test
    @DisplayName("팔로워 수를 정상적으로 반환해야 한다")
    void getFollowerCount_shouldReturnCount() {
        // given
        when(followRepository.countByFollower(follower)).thenReturn(10);

        // when
        int count = followQueryService.getFollowerCount(follower);

        // then
        assertThat(count).isEqualTo(10);
    }

    @Test
    @DisplayName("팔로잉 수를 정상적으로 반환해야 한다")
    void getFollowingCount_shouldReturnCount() {
        // given
        when(followRepository.countByFollowing(following)).thenReturn(5);

        // when
        int count = followQueryService.getFollowingCount(following);

        // then
        assertThat(count).isEqualTo(5);
    }
}
