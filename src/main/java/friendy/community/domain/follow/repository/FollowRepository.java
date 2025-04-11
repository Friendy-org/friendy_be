package friendy.community.domain.follow.repository;

import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.query.FollowQueryRepository;
import friendy.community.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long>, FollowQueryRepository {
    boolean existsByFollowerAndFollowing(Member follower, Member following);

    Follow findByFollowerAndFollowing(Member follower, Member following);

    int countByFollowing(Member following);

    int countByFollower(Member follower);
}
