package friendy.community.domain.follow.model;

import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class FollowTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Follow 객체가 정상적으로 생성되는지 테스트")
    void createFollow() {
        // Given
        List<Member> members = MemberFixture.createMultipleMembers(2);
        List<Member> savedMembers = memberRepository.saveAll(members);
        Member follower = savedMembers.get(0);
        Member following = savedMembers.get(1);

        // When
        Follow follow = Follow.of(follower, following);

        // Then
        assertThat(follow).isNotNull();
        assertThat(follow.getFollower().getId()).isEqualTo(follower.getId());
        assertThat(follow.getFollowing().getId()).isEqualTo(following.getId());
    }
}