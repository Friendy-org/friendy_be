package friendy.community.domain.follow.service;

import friendy.community.domain.follow.model.Follow;
import friendy.community.domain.follow.repository.FollowRepository;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowCommandService {

    private final MemberDomainService memberDomainService;
    private final FollowDomainService followDomainService;
    private final FollowRepository followRepository;

    public void follow(Long memberId, Long targetId) {
        Member requester = memberDomainService.getMemberById(memberId);
        Member target = memberDomainService.getMemberById(targetId);

        followDomainService.validateFollowable(requester, target);
        Follow follow = Follow.of(requester, target);
        followRepository.save(follow);
    }

    public void unfollow(Long memberId, Long targetId) {
        Member requester = memberDomainService.getMemberById(memberId);
        Member target = memberDomainService.getMemberById(targetId);
        followDomainService.validateUnfollowable(requester, target);

        Follow follow = followRepository.findByFollowerAndFollowing(requester, target);
        followRepository.delete(follow);
    }
}
