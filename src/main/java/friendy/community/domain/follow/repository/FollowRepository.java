package friendy.community.domain.follow.repository;

import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(Member follower, Member following);

    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);

    long countByFollowing(Member following);

    long countByFollower(Member follower);
}
