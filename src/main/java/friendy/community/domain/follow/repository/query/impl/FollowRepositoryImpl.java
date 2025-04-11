package friendy.community.domain.follow.repository.query.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.query.FollowQueryRepository;
import friendy.community.domain.member.model.QMember;
import friendy.community.domain.member.model.QMemberImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static friendy.community.domain.follow.model.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Follow> findFollowingFollows(Long memberId, Long lastFollowingId, int size) {
        return queryFactory
            .selectFrom(follow)
            .join(follow.following, QMember.member).fetchJoin()
            .leftJoin(QMember.member.memberImage, QMemberImage.memberImage).fetchJoin()
            .where(
                follow.follower.id.eq(memberId),
                lastFollowingId != null ? follow.id.lt(lastFollowingId) : null
            )
            .orderBy(follow.id.desc())
            .limit(size + 1)
            .fetch();
    }

    public List<Follow> findFollowerFollows(Long memberId, Long lastFollowerId, int size) {
        return queryFactory
            .selectFrom(follow)
            .join(follow.follower, QMember.member).fetchJoin()
            .leftJoin(QMember.member.memberImage, QMemberImage.memberImage).fetchJoin()
            .where(
                follow.following.id.eq(memberId),
                lastFollowerId != null ? follow.id.lt(lastFollowerId) : null
            )
            .orderBy(follow.id.desc())
            .limit(size + 1)
            .fetch();
    }
}