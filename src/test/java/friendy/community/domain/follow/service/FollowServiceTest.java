package friendy.community.domain.follow.service;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.member.service.MemberService;
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

import java.util.List;

import static friendy.community.domain.auth.fixtures.TokenFixtures.CORRECT_ACCESS_TOKEN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext
class FollowServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FollowService followService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private EntityManager entityManager;

    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    void setup() {
        resetMemberIdSequence();
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Authorization", CORRECT_ACCESS_TOKEN);

        Member member = MemberFixture.memberFixture();
        memberService.signUp(new MemberSignUpRequest(member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate(), null));

    }

    private void resetMemberIdSequence() {
        entityManager.createNativeQuery("ALTER TABLE member AUTO_INCREMENT = 1").executeUpdate();
    }

    @Test
    @DisplayName("팔로우 성공 테스트")
    void followSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();
        Member follower = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // When
        followService.follow(httpServletRequest, following.getId());

        //Then
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이미 팔로우한 경우 예외 발생 테스트")
    void alreadyFollowed() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();
        followService.follow(httpServletRequest, following.getId());

        // When & Then
        assertThatThrownBy(() -> followService.follow(httpServletRequest, following.getId()))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("이미 팔로우한 회원입니다.");
    }

    @Test
    @DisplayName("언팔로우 성공 테스트")
    void unfollowSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member follower = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Member following = savedMembers.getFirst();
        followService.follow(httpServletRequest, following.getId());

        // When
        followService.unfollow(httpServletRequest, following.getId());

        // Then
        assertThat(followRepository.existsByFollowerAndFollowing(follower, following)).isFalse();
    }

    @Test
    @DisplayName("언팔로우 실패 - 이미 언팔로우된 상태")
    void unfollowFailAlreadyUnfollowed() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();

        // When & Then
        assertThatThrownBy(() -> followService.unfollow(httpServletRequest, following.getId()))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("팔로우하지 않은 회원입니다.");
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 테스트")
    void getFollowingListSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(12));
        Member follower = savedMembers.get(0);
        for (int i = 1; i < savedMembers.size(); i++) {
            Member following = savedMembers.get(i);
            followRepository.save(Follow.of(follower, following));
        }

        // When
        FollowListResponse firstPage = followService.getFollowingMembers(follower.getId(), 0L, 10);
        FollowListResponse secondPage = followService.getFollowingMembers(follower.getId(), 10L, 10);

        // Then
        assertThat(firstPage.members().size()).isEqualTo(10);
        assertThat(secondPage.members().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 테스트")
    void getFollowerListSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(12));
        Member following = savedMembers.get(0);
        for (int i = 1; i < savedMembers.size(); i++) {
            Member follower = savedMembers.get(i);
            followRepository.save(Follow.of(follower, following));
        }

        // When
        FollowListResponse firstPage = followService.getFollowerMembers(following.getId(), 0L, 10);
        FollowListResponse secondPage = followService.getFollowerMembers(following.getId(), 10L, 10);

        // Then
        assertThat(firstPage.members().size()).isEqualTo(10);
        assertThat(secondPage.members().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("맞팔로우 여부 테스트")
    void mutualFollowTest() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(2));
        Member member1 = savedMembers.get(0);
        Member member2 = savedMembers.get(1);

        // When & Then
        followRepository.save(Follow.of(member1, member2));
        followRepository.save(Follow.of(member2, member1));
        boolean mutualFollow1 = followService.isMutualFollow(member1.getId(), member2.getId());
        assertThat(mutualFollow1).isTrue();

        // When & Then
        followRepository.deleteAllInBatch();
        followRepository.save(Follow.of(member1, member2));
        boolean mutualFollow2 = followService.isMutualFollow(member1.getId(), member2.getId());
        assertThat(mutualFollow2).isFalse();

        // When & Then
        followRepository.deleteAllInBatch();
        boolean mutualFollow3 = followService.isMutualFollow(member1.getId(), member2.getId());
        assertThat(mutualFollow3).isFalse();
    }

    @Test
    @DisplayName("팔로우 수 조회 성공 테스트")
    void getFollowCountSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(3));
        Member member1 = savedMembers.get(0);
        Member member2 = savedMembers.get(1);
        Member member3 = savedMembers.get(2);

        followRepository.save(Follow.of(member1, member2));
        followRepository.save(Follow.of(member1, member3));
        followRepository.save(Follow.of(member2, member1));
        followRepository.save(Follow.of(member3, member1));

        // When
        long followingCount = followService.getFollowingCount(member1.getId());
        long followerCount = followService.getFollowerCount(member1.getId());

        // Then
        assertThat(followingCount).isEqualTo(2);
        assertThat(followerCount).isEqualTo(2);
    }

    @Test
    @DisplayName("팔로잉/팔로워 목록 조회 예외 - 존재하지 않는 회원")
    void getFollowListExceptionNonExistentMember() {
        // Given
        Long nonExistentMemberId = 999L;

        // When & Then
        assertThatThrownBy(() -> followService.getFollowingMembers(nonExistentMemberId, 0L, 10))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");

        assertThatThrownBy(() -> followService.getFollowerMembers(nonExistentMemberId, 0L, 10))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("팔로우 및 팔로우 수 조회 예외 - 존재하지 않는 회원 또는 자기 자신")
    void followExceptionInvalidMember() {
        // When & Then
        assertThatThrownBy(() -> followService.follow(httpServletRequest, 1L))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("자기 자신을 대상으로 수행할 수 없습니다.");

        assertThatThrownBy(() -> followService.follow(httpServletRequest, 999L))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");

        assertThatThrownBy(() -> followService.getFollowerCount(999L))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");

        assertThatThrownBy(() -> followService.getFollowingCount(999L))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");
    }
}