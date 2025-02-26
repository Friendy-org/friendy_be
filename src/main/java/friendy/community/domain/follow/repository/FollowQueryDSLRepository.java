package friendy.community.domain.follow.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.dto.response.FollowMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static friendy.community.domain.follow.model.QFollow.follow;
import static friendy.community.domain.member.model.QMember.member;
import static friendy.community.domain.member.model.QMemberImage.memberImage;

@Repository
@RequiredArgsConstructor
public class FollowQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    public FollowListResponse findFollowMembers(Long memberId, Long cursor, int pageSize) {
        List<FollowMemberResponse> members = queryFactory
            .select(Projections.constructor(
                FollowMemberResponse.class,
                member.id,
                member.nickname,
                memberImage.imageUrl
            ))
            .from(follow)
            .join(follow.following, member)
            .leftJoin(member.memberImage, memberImage)
            .where(
                follow.follower.id.eq(memberId),
                cursor != null ? follow.id.lt(cursor) : null
            )
            .orderBy(follow.id.desc())  // `_super` 없이 바로 사용
            .limit(pageSize + 1)
            .fetch();
        Long nextCursor = members.size() > pageSize ? members.remove(pageSize).memberId() : null;

        return new FollowListResponse(members, nextCursor);
    }
}
