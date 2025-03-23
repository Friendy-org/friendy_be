package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowService;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.security.FriendyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController implements SpringDocFollowController {
    private final FollowService followService;

    @PostMapping("/{targetId}")
    public ResponseEntity<FriendyResponse<Void>> follow(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @PathVariable final Long targetId
    ) {
        followService.follow(userDetails.getMemberId(), targetId);
        return ResponseEntity.ok(FriendyResponse.of(200, "팔로잉 성공"));
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<FriendyResponse<Void>> unfollow(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @PathVariable Long targetId
    ) {
        followService.unfollow(userDetails.getMemberId(), targetId);
        return ResponseEntity.ok(FriendyResponse.of(200, "언팔로우 성공"));
    }

    @GetMapping("/following/{targetId}")
    public ResponseEntity<FriendyResponse<FollowListResponse>> getFollowingMembers(
            @PathVariable final Long targetId,
            @RequestParam(required = false) Long lastFollowingId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(200,
                "팔로잉 목록 조회 성공",
                followService.getFollowingMembers(targetId, lastFollowingId)));
    }

    @GetMapping("/follower/{targetId}")
    public ResponseEntity<FriendyResponse<FollowListResponse>> getFollowerMembers(
            @PathVariable final Long targetId,
            @RequestParam(required = false) Long lastFollowerId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(200,
                "팔로우 목록 조회 성공",
                followService.getFollowerMembers(targetId, lastFollowerId)));
    }
}
