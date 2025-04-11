package friendy.community.domain.member.service;

import friendy.community.domain.follow.service.FollowQueryService;
import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.domain.member.dto.response.FindMemberPostsResponse;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.model.Member;
import friendy.community.domain.member.repository.MemberRepository;
import friendy.community.domain.post.service.PostQueryService;
import friendy.community.global.exception.domain.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final PostQueryService postQueryService;
    private final FollowQueryService followQueryService;

    public FindMemberResponse getMemberInfo(final Long requesterId, Long memberId) {
        final Member profileMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION));

        boolean isMe = profileMember.getId().equals(requesterId);
        int followerCount = followQueryService.getFollowerCount(profileMember);
        int followingCount = followQueryService.getFollowingCount(profileMember);

        return FindMemberResponse.from(profileMember, isMe, followerCount, followingCount);
    }

    public FindMemberPostsResponse getMemberPosts(final Long memberId, final Long lastPostId) {
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException(MemberExceptionCode.USER_NOT_FOUND_EXCEPTION);
        }
        return postQueryService.getMemberPosts(memberId, lastPostId);
    }
}