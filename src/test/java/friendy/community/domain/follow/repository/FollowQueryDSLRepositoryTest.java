package friendy.community.domain.follow.repository;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.dto.response.FollowMemberResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FollowQueryDSLRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FollowQueryDSLRepository followQueryDSLRepository;

    @Test
    @DisplayName("findFollowMembers: 팔로워가 특정 회원을 팔로우한 경우, 페이징 처리된 팔로잉 목록 조회")
    void testFindFollowingMembersWithCursor() {
        // given
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(3));
        Member follower = savedMembers.get(0);
        Member following1 = savedMembers.get(1);
        Member following2 = savedMembers.get(2);

        followRepository.save(Follow.of(follower, following1));
        followRepository.save(Follow.of(follower, following2));

        // when
        FollowListResponse firstPage = followQueryDSLRepository.findFollowingMembers(follower.getId(), null, 1);

        // then
        assertThat(firstPage.members()).hasSize(1);

        FollowMemberResponse firstFollow = firstPage.members().getFirst();
        assertThat(firstFollow.memberId()).isEqualTo(following2.getId());

        Long nextCursor = firstPage.nextCursor();
        // when
        FollowListResponse secondPage = followQueryDSLRepository.findFollowingMembers(follower.getId(), nextCursor, 1);

        // then
        assertThat(secondPage.members()).hasSize(1);

        FollowMemberResponse secondFollow = secondPage.members().get(0);
        assertThat(secondFollow.memberId()).isEqualTo(following1.getId());
    }

    @Test
    @DisplayName("findFollowerMembers: 특정 회원을 팔로우한 경우, 페이징 처리된 팔로워 목록 조회")
    void testFindFollowerMembersWithCursor() {
        // given: 두 회원 생성 후 저장
        List<Member> savedMembers = memberRepository.saveAll(MemberFixture.createMultipleMembers(3));
        Member follower1 = savedMembers.get(0);
        Member follower2 = savedMembers.get(1);
        Member following = savedMembers.get(2);


        followRepository.save(Follow.of(follower1, following));
        followRepository.save(Follow.of(follower2, following));

        // when - 첫 번째 페이지 조회 (cursor 없이 최신 순서 조회)
        FollowListResponse firstPage = followQueryDSLRepository.findFollowerMembers(following.getId(), null, 1);

        // then - 첫 번째 페이지에는 1개의 결과가 있어야 함
        assertThat(firstPage.members()).hasSize(1);

        FollowMemberResponse firstFollow = firstPage.members().getFirst();
        assertThat(firstFollow.memberId()).isEqualTo(follower2.getId()); // 최신 팔로워가 먼저 나와야 함

        Long nextCursor = firstPage.nextCursor();  //1l

        // when - 두 번째 페이지 조회 (cursor 사용)
        FollowListResponse secondPage = followQueryDSLRepository.findFollowerMembers(following.getId(), nextCursor, 1);

        // then - 두 번째 페이지에도 1개의 결과가 있어야 함 (이전 팔로워 정보)
        assertThat(secondPage.members()).hasSize(1);

        FollowMemberResponse secondFollow = secondPage.members().get(0);
        assertThat(secondFollow.memberId()).isEqualTo(follower1.getId()); // 그다음 회원이 나와야 함
    }

}