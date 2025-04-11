package friendy.community.domain.follow.service;

import friendy.community.domain.follow.controller.code.FollowExceptionCode;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.global.exception.domain.BadRequestException;
import friendy.community.global.exception.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowDomainService {

    private final FollowRepository followRepository;

    public void validateFollowable(Member follower, Member following) {
        validateSelfFollow(follower, following);
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new BadRequestException(FollowExceptionCode.ALREADY_FOLLOWED);
        }
    }

    public void validateUnfollowable(Member follower, Member following) {
        validateSelfFollow(follower, following);
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new BadRequestException(FollowExceptionCode.NOT_FOLLOWED);
        }
    }

    public void validateSelfFollow(Member follower, Member following) {
        if (follower.getId().equals(following.getId())) {
            throw new BadRequestException(FollowExceptionCode.SELF_FOLLOW_NOT_ALLOWED);
        }
    }

    public void validateFollowingExists(List<Follow> follows) {
        if (follows.isEmpty()) {
            throw new NotFoundException(FollowExceptionCode.FOLLOWING_MEMBER_NOT_FOUND);
        }
    }

    public void validateFollowerExists(List<Follow> follows) {
        if (follows.isEmpty()) {
            throw new NotFoundException(FollowExceptionCode.FOLLOWER_MEMBER_NOT_FOUND);
        }
    }
}
