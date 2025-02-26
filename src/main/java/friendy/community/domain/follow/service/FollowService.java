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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        Member following = memberRepository.findById(targetId)
            .orElseThrow(() -> new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "해당 ID의 회원을 찾을 수 없습니다."));

        Follow follow = Follow.of(follower,following);
        followRepository.save(follow);
    }

    public FollowListResponse getFollowingMembers(Long memberId, Long cursor, int pageSize) {
        return followQueryDSLRepository.findFollowMembers(memberId, cursor, pageSize);
    }
}
