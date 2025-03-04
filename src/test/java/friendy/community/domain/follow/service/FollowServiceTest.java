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
    void 팔로우_성공() {
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();
        Member follower = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        followService.follow(httpServletRequest, following.getId());

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        assertThat(exists).isTrue();
    }

    @Test
    void 이미팔로우() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();
        followService.follow(httpServletRequest, following.getId());

        // When & Then
        assertThatThrownBy(() -> followService.follow(httpServletRequest, following.getId())).isInstanceOf(FriendyException.class).hasMessageContaining("이미 팔로우한 회원입니다.");
    }

    @Test
    void 언팔로우_성공() {
        // Given: 팔로우한 상태
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member follower = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Member following = savedMembers.getFirst();
        followService.follow(httpServletRequest, following.getId());

        // When: 언팔로우 실행
        followService.unfollow(httpServletRequest, following.getId());

        // Then: 팔로우 관계가 삭제됨
        assertThat(followRepository.existsByFollowerAndFollowing(follower, following)).isFalse();
    }

    @Test
    void 언팔로우_실패_이미_언팔로우된_상태() {
        // Given: 먼저 언팔로우 실행
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();

        // When & Then: 이미 언팔로우된 상태에서 다시 언팔로우 시도 → 예외 발생
        assertThatThrownBy(() -> followService.unfollow(httpServletRequest, following.getId())).isInstanceOf(FriendyException.class).hasMessageContaining("팔로우하지 않은 회원입니다.");
    }

    @Test
    void 팔로잉_목록_조회_성공() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(25));
        Member follower = savedMembers.get(0); // 첫 번째 회원이 나머지 24명을 팔로우
        for (int i = 1; i < savedMembers.size(); i++) {
            Member following = savedMembers.get(i);
            followRepository.save(Follow.of(follower, following));
        }
        // When
        FollowListResponse response = followService.getFollowingMembers(follower.getId(), 0L, 10);
        // Then
        assertThat(response.members().size()).isEqualTo(10); // 10개인지 확인
    }

    @Test
    void 팔로워_목록_조회_성공() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(25));
        Member following = savedMembers.get(0); // 첫 번째 회원이 나머지 24명의 팔로워가 됨
        for (int i = 1; i < savedMembers.size(); i++) {
            Member follower = savedMembers.get(i);
            followRepository.save(Follow.of(follower, following));
        }

        // When
        FollowListResponse response = followService.getFollowerMembers(following.getId(), 0L, 10);

        // Then
        assertThat(response.members()).isNotNull();
        assertThat(response.members().size()).isEqualTo(10); // 한 페이지당 10명이 반환되는지 확인
    }

    @Test
    void 팔로잉_목록_조회_예외_존재하지_않는_회원() {
        // Given
        Long nonExistentMemberId = 999L; // 존재하지 않는 ID

        // When & Then
        assertThatThrownBy(() -> followService.getFollowingMembers(nonExistentMemberId, 0L, 10)).isInstanceOf(FriendyException.class).hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");

        assertThatThrownBy(() -> followService.getFollowerMembers(nonExistentMemberId, 0L, 10)).isInstanceOf(FriendyException.class).hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");
    }

    @Test
    void 팔로워_목록_조회_예외_존재하지_않는_회원() {
        // Given
        // When & Then
        assertThatThrownBy(() -> followService.follow(httpServletRequest, 1L)).isInstanceOf(FriendyException.class).hasMessageContaining("자기 자신을 대상으로 수행할 수 없습니다.");

        assertThatThrownBy(() -> followService.follow(httpServletRequest, 999L)).isInstanceOf(FriendyException.class).hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");

        assertThatThrownBy(() -> followService.getFollowerCount(999L)).isInstanceOf(FriendyException.class).hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");

        assertThatThrownBy(() -> followService.getFollowingCount(999L)).isInstanceOf(FriendyException.class).hasMessageContaining("해당 ID의 회원을 찾을 수 없습니다.");
    }

    @Test
    void 맞팔로우_여부_테스트() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(2));
        Member member1 = savedMembers.get(0);
        Member member2 = savedMembers.get(1);

        // Case 1: 맞팔로우 (서로 팔로우)
        followRepository.save(Follow.of(member1, member2));
        followRepository.save(Follow.of(member2, member1));
        boolean mutualFollow1 = followService.isMutualFollow(member1.getId(), member2.getId());
        assertThat(mutualFollow1).isTrue();

        // Case 2: 일방향 팔로우 (member1 → member2)
        followRepository.deleteAllInBatch();
        followRepository.save(Follow.of(member1, member2));
        boolean mutualFollow2 = followService.isMutualFollow(member1.getId(), member2.getId());
        assertThat(mutualFollow2).isFalse();

        // Case 3: 맞팔로우 없음 (아무도 팔로우 안 함)
        followRepository.deleteAllInBatch();
        boolean mutualFollow3 = followService.isMutualFollow(member1.getId(), member2.getId());
        assertThat(mutualFollow3).isFalse();
    }

    @Test
    void 팔로우_수_조회_성공() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(3));
        Member member1 = savedMembers.get(0); // 테스트 대상
        Member member2 = savedMembers.get(1);
        Member member3 = savedMembers.get(2);

        followRepository.save(Follow.of(member1, member2)); // member1 → member2 팔로우
        followRepository.save(Follow.of(member1, member3)); // member1 → member3 팔로우
        followRepository.save(Follow.of(member2, member1)); // member2 → member1 팔로우
        followRepository.save(Follow.of(member3, member1)); // member3 → member1 팔로우

        // When
        long followingCount = followService.getFollowingCount(member1.getId()); // member1이 팔로우한 사람 수
        long followerCount = followService.getFollowerCount(member1.getId()); // member1을 팔로우한 사람 수

        // Then
        assertThat(followingCount).isEqualTo(2); // member1이 2명을 팔로우함
        assertThat(followerCount).isEqualTo(2); // member1이 2명에게 팔로우됨
    }
}