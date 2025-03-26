package friendy.community.domain.follow.service;

import friendy.community.domain.follow.controller.code.FollowExceptionCode;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.member.service.MemberService;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

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

    private Member member;

    @BeforeEach
    void setup() {
        resetMemberIdSequence();

        member = MemberFixture.memberFixture();
        Long memberId = memberService.signup(new MemberSignUpRequest(member.getEmail(), member.getNickname(), member.getPassword(), member.getBirthDate(), null));
        member = memberService.findMemberById(memberId);
    }

    private void resetMemberIdSequence() {
        followRepository.deleteAll();
        memberRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE member AUTO_INCREMENT = 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE follow AUTO_INCREMENT = 1").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("팔로우 성공 테스트")
    void followSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member following = savedMembers.getFirst();
        Member follower = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // When
        followService.follow(follower.getId(), following.getId());

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
        followService.follow(member.getId(), following.getId());

        // When & Then
        assertThatThrownBy(() -> followService.follow(member.getId(), following.getId()))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.ALREADY_FOLLOWED);
    }

    @Test
    @DisplayName("언팔로우 성공 테스트")
    void unfollowSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(1));
        Member follower = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Member following = savedMembers.getFirst();
        followService.follow(follower.getId(), following.getId());

        // When
        followService.unfollow(follower.getId(), following.getId());

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
        assertThatThrownBy(() -> followService.unfollow(member.getId(), following.getId()))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.NOT_FOLLOWED);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 테스트")
    void getFollowingListSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(25));
        Member follower = savedMembers.get(0);

        for (int i = 1; i < savedMembers.size(); i++) {
            Member following = savedMembers.get(i);
            followRepository.save(Follow.of(follower, following));
        }

        // When
        FollowListResponse firstPage = followService.getFollowingMembers(follower.getId(), null);
        FollowListResponse secondPage = followService.getFollowingMembers(follower.getId(), firstPage.lastFollowId());

        // Then
        assertThat(firstPage.members().size()).isEqualTo(20);
        assertThat(secondPage.members().size()).isEqualTo(4);
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 테스트")
    void getFollowerListSuccess() {
        // Given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(25));
        Member following = savedMembers.get(0);
        for (int i = 1; i < savedMembers.size(); i++) {
            Member follower = savedMembers.get(i);
            followRepository.save(Follow.of(follower, following));
        }

        // When
        FollowListResponse firstPage = followService.getFollowerMembers(following.getId(), null);
        FollowListResponse secondPage = followService.getFollowerMembers(following.getId(), firstPage.lastFollowId());

        // Then
        assertThat(firstPage.members().size()).isEqualTo(20);
        assertThat(secondPage.members().size()).isEqualTo(4);
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
        assertThatThrownBy(() -> followService.getFollowingMembers(nonExistentMemberId, 0L ))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.MEMBER_NOT_FOUND);
        assertThatThrownBy(() -> followService.getFollowerMembers(nonExistentMemberId, 0L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("팔로잉/팔로워가 없을 경우 예외가 발생한다.")
    void testGetFollowingAndFollowerMembersThrowsExceptionWhenNoMembers() {
        // When & Then
        assertThatThrownBy(() -> followService.getFollowingMembers(1L, 0L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.FOLLOWING_MEMBER_NOT_FOUND);

        assertThatThrownBy(() -> followService.getFollowerMembers(1L, 0L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.FOLLOWER_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("팔로우 및 팔로우 수 조회 예외 - 존재하지 않는 회원 또는 자기 자신")
    void followExceptionInvalidMember() {
        // When & Then
        assertThatThrownBy(() -> followService.follow(member.getId(), 1L))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.SELF_FOLLOW_NOT_ALLOWED);

        assertThatThrownBy(() -> followService.follow(member.getId(), 999L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.MEMBER_NOT_FOUND);

        assertThatThrownBy(() -> followService.getFollowerCount(999L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.MEMBER_NOT_FOUND);

        assertThatThrownBy(() -> followService.getFollowingCount(999L))
            .isInstanceOf(NotFoundException.class)
            .hasFieldOrPropertyWithValue("exceptionType", FollowExceptionCode.MEMBER_NOT_FOUND);
    }
}