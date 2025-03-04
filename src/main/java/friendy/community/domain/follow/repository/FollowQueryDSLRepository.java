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

    public FollowListResponse findFollowingMembers(Long memberId, Long startIndex, int pageSize) {
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
            .where(follow.follower.id.eq(memberId))
            .orderBy(follow.createdDate.desc())
            .offset(startIndex)
            .limit(pageSize + 1)
            .fetch();

        boolean hasNext = members.size() > pageSize;
        if (hasNext) {
            members.remove(pageSize);
        }

        return new FollowListResponse(members, hasNext);
    }

    public FollowListResponse findFollowerMembers(Long memberId, Long startIndex, int pageSize) {
        List<FollowMemberResponse> members = queryFactory
            .select(Projections.constructor(
                FollowMemberResponse.class,
                member.id,
                member.nickname,
                memberImage.imageUrl
            ))
            .from(follow)
            .join(follow.follower, member)
            .leftJoin(member.memberImage, memberImage)
            .where(follow.following.id.eq(memberId))
            .orderBy(follow.createdDate.desc())
            .offset(startIndex)
            .limit(pageSize + 1)
            .fetch();

        boolean hasNext = members.size() > pageSize;
        if (hasNext) {
            members.remove(pageSize);
        }

        return new FollowListResponse(members, hasNext);
    }
}
