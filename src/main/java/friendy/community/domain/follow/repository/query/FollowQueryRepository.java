package friendy.community.domain.follow.repository.query;

import friendy.community.domain.follow.model.Follow;

import java.util.List;

public interface FollowQueryRepository {
    List<Follow> findFollowingFollows(Long memberId, Long lastFollowingId, int size);
    List<Follow> findFollowerFollows(Long memberId, Long lastFollowerId, int size);
}