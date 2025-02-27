package friendy.community.domain.follow.service;

import friendy.community.domain.auth.jwt.JwtTokenExtractor;
import friendy.community.domain.auth.jwt.JwtTokenProvider;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowQueryDSLRepository;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final JwtTokenExtractor jwtTokenExtractor;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final FollowQueryDSLRepository followQueryDSLRepository;

    public void follow(final HttpServletRequest httpServletRequest, final Long targetId) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        Member follower = authService.getMemberByEmail(email);

        Member following = getValidTargetMember(follower, targetId);

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new FriendyException(ErrorCode.ALREADY_EXISTS, "이미 팔로우한 회원입니다.");
        }

        Follow follow = Follow.of(follower, following);
        followRepository.save(follow);
    }

    public void unfollow(final HttpServletRequest httpServletRequest, final Long targetId) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        final String email = jwtTokenProvider.extractEmailFromAccessToken(accessToken);
        Member follower = authService.getMemberByEmail(email);
        Member following = getValidTargetMember(follower, targetId);

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
            .orElseThrow(() -> new FriendyException(ErrorCode.ALREADY_EXISTS, "팔로우하지 않은 회원입니다."));

        followRepository.delete(follow);
    }

    public FollowListResponse getFollowingMembers(final Long targetId, Long cursor, int pageSize) {
        if (!memberRepository.existsById(targetId)) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다.");
        }

        return followQueryDSLRepository.findFollowMembers(targetId, cursor, pageSize);
    }

    public FollowListResponse getFollowerMembers(final Long targetId, Long cursor, int pageSize) {
        if (!memberRepository.existsById(targetId)) {
            throw new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다.");
        }

        return followQueryDSLRepository.findFollowerMembers(targetId, cursor, pageSize);
    }

    @Transactional(readOnly = true)
    public boolean isMutualFollow(Long memberId, Long targetId) {
        boolean isFollowing = followRepository.existsByFollowerAndFollowing(memberRepository.getReferenceById(memberId), memberRepository.getReferenceById(targetId));
        boolean isFollowedBy = followRepository.existsByFollowerAndFollowing(memberRepository.getReferenceById(targetId), memberRepository.getReferenceById(memberId));
        return isFollowing && isFollowedBy;
    }

    private Member getValidTargetMember(Member requester, Long targetId) {
        if (requester.getId().equals(targetId)) {
            throw new FriendyException(ErrorCode.INVALID_REQUEST, "자기 자신을 대상으로 수행할 수 없습니다.");
        }

        return memberRepository.findById(targetId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다."));
    }
}
