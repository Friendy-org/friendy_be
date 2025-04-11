package friendy.community.domain.follow.repository.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.member.fixture.MemberFixture;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.config.TestQuerydslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestQuerydslConfig.class)
class FollowRepositoryImplTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private FollowRepositoryImpl followRepositoryImpl;

    @Autowired
    private MemberRepository memberRepository;

    private Member memberA;
    private Member memberB;
    private Member memberC;

    @BeforeEach
    void setUp() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        followRepositoryImpl = new FollowRepositoryImpl(queryFactory);
        List<Member> members = MemberFixture.createMultipleMembers(3);

        memberA = members.get(0);
        memberB = members.get(1);
        memberC = members.get(2);
        memberRepository.saveAll(members);

        em.persist(Follow.of(memberA, memberB));
        em.persist(Follow.of(memberA, memberC));
        em.persist(Follow.of(memberB, memberA));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("팔로잉 목록 조회")
    void findFollowingFollows() {
        // given
        List<Follow> fullResult = followRepositoryImpl.findFollowingFollows(memberA.getId(), null, 10);
        Long lastId = fullResult.get(0).getId();

        // when
        List<Follow> result = followRepositoryImpl.findFollowingFollows(memberA.getId(), lastId, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFollowing().getNickname()).isEqualTo("nickname2");
    }

    @Test
    @DisplayName("팔로워 목록 조회")
    void findFollowerFollows() {
        // given
        List<Follow> fullResult = followRepositoryImpl.findFollowerFollows(memberA.getId(), null, 10);
        Long lastId = fullResult.get(0).getId();

        // when
        List<Follow> result = followRepositoryImpl.findFollowerFollows(memberA.getId(), lastId, 10);

        // then
        assertThat(result).isEmpty();
    }
}
