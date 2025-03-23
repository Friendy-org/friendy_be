package friendy.community.domain.follow.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.dto.response.FollowMemberResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.member.model.QMember;
import friendy.community.domain.member.model.QMemberImage;
import friendy.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static friendy.community.domain.follow.model.QFollow.follow;

@Repository
@RequiredArgsConstructor
public class FollowQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    public FollowListResponse findFollowingMembers(Long memberId, Long lastFollowingId, int size) {
        List<Follow> follows = queryFactory
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

        if (follows.isEmpty()) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "팔로잉멤버가 없습니다.");
        }

        boolean hasNext = follows.size() > size;
        if (hasNext) {
            follows.removeLast();
        }

        Long newLastFollowId = follows.getLast().getId();

        List<FollowMemberResponse> followMemberResponses = follows.stream()
            .map(follow -> FollowMemberResponse.from(follow.getFollowing()))
            .collect(Collectors.toList());

        return new FollowListResponse(followMemberResponses, hasNext, newLastFollowId);
    }

    public FollowListResponse findFollowerMembers(Long memberId, Long lastFollowerId, int size) {
        List<Follow> follows = queryFactory
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

        if (follows.isEmpty()) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "팔로워가 없습니다.");
        }


        boolean hasNext = follows.size() > size;
        if (hasNext) {
            follows.remove(follows.size() - 1);
        }

        Long newLastFollowerId = follows.getLast().getId();

        List<FollowMemberResponse> followMemberResponses = follows.stream()
            .map(follow -> FollowMemberResponse.from(follow.getFollower()))
            .collect(Collectors.toList());

        return new FollowListResponse(followMemberResponses, hasNext, newLastFollowerId);
    }
}
