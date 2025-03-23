package friendy.community.domain.follow.service;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowQueryDSLRepository;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.member.service.MemberService;
import friendy.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final FollowQueryDSLRepository followQueryDSLRepository;
    private final MemberService memberService;

    public void follow(final Long memberId, final Long targetId) {

        Member follower = memberService.findMemberById(memberId);

        Member following = getValidTargetMember(follower, targetId);

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new FriendyException(ErrorCode.ALREADY_EXISTS, "이미 팔로우한 회원입니다.");
        }

        Follow follow = Follow.of(follower, following);
        followRepository.save(follow);
    }

    public void unfollow(final Long memberId, final Long targetId) {

        Member follower = memberService.findMemberById(memberId);
        Member following = getValidTargetMember(follower, targetId);

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
            .orElseThrow(() -> new FriendyException(ErrorCode.ALREADY_EXISTS, "팔로우하지 않은 회원입니다."));

        followRepository.delete(follow);
    }

    public FollowListResponse getFollowingMembers(final Long targetId, final Long lastFollowingId ) {
        if (!memberRepository.existsById(targetId)) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다.");
        }

        return followQueryDSLRepository.findFollowingMembers(targetId, lastFollowingId, 20);
    }

    public FollowListResponse getFollowerMembers(final Long targetId, final Long lastFollowerId) {
        if (!memberRepository.existsById(targetId)) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다.");
        }

        return followQueryDSLRepository.findFollowerMembers(targetId, lastFollowerId, 20);
    }

    @Transactional(readOnly = true)
    public boolean isMutualFollow(Long memberId, Long targetId) {
        boolean isFollowing = followRepository.existsByFollowerAndFollowing(memberRepository.getReferenceById(memberId), memberRepository.getReferenceById(targetId));
        boolean isFollowedBy = followRepository.existsByFollowerAndFollowing(memberRepository.getReferenceById(targetId), memberRepository.getReferenceById(memberId));
        return isFollowing && isFollowedBy;
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다."));
        return followRepository.countByFollower(member);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다."));
        return followRepository.countByFollowing(member);
    }

    private Member getValidTargetMember(Member requester, Long memberId) {
        if (requester.getId().equals(memberId)) {
            throw new FriendyException(ErrorCode.INVALID_REQUEST, "자기 자신을 대상으로 수행할 수 없습니다.");
        }

        return memberRepository.findById(memberId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다."));
    }
}
