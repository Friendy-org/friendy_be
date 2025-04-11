package friendy.community.domain.follow.service;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.dto.response.FollowMemberResponse;
import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final FollowDomainService followDomainService;

    public FollowListResponse getFollowingList(Long memberId, Long lastId) {
        List<Follow> follows = followRepository.findFollowingFollows(memberId, lastId, 20);

        followDomainService.validateFollowingExists(follows);
        boolean hasNext = follows.size() > 20;
        if (hasNext) {
            follows.remove(follows.size() - 1);
        }

        Long newLastFollowId = follows.get(follows.size() - 1).getId();
        List<FollowMemberResponse> members = follows.stream()
            .map(f -> FollowMemberResponse.from(f.getFollowing()))
            .collect(Collectors.toList());

        return new FollowListResponse(members, hasNext, newLastFollowId);
    }

    public FollowListResponse getFollowerList(Long memberId, Long lastId) {
        List<Follow> follows = followRepository.findFollowerFollows(memberId, lastId, 20);
        followDomainService.validateFollowerExists(follows);
        boolean hasNext = follows.size() > 20;
        if (hasNext) {
            follows.remove(follows.size() - 1);
        }

        Long newLastFollowId = follows.get(follows.size() - 1).getId();
        List<FollowMemberResponse> members = follows.stream()
            .map(f -> FollowMemberResponse.from(f.getFollower()))
            .collect(Collectors.toList());

        return new FollowListResponse(members, hasNext, newLastFollowId);
    }

    public int getFollowerCount(Member member) {
        return followRepository.countByFollower(member);
    }

    public int getFollowingCount(Member member) {
        return followRepository.countByFollowing(member);
    }
}

