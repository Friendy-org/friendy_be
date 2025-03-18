package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowService;
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
    public ResponseEntity<Void> follow(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable final Long targetId
    ) {
        followService.follow(userDetails.getMemberId(), targetId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> unfollow(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long targetId
    ) {
        followService.unfollow(userDetails.getMemberId(), targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/following/{targetId}")
    public ResponseEntity<FollowListResponse> getFollowingMembers(
        @PathVariable final Long targetId,
        @RequestParam(required = false) Long lastFollowingId
    ) {
        FollowListResponse response = followService.getFollowingMembers(targetId, lastFollowingId, 3);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/follower/{targetId}")
    public ResponseEntity<FollowListResponse> getFollowerMembers(
        @PathVariable final Long targetId,
        @RequestParam(required = false) Long lastFollowerId
    ) {
        FollowListResponse response = followService.getFollowerMembers(targetId, lastFollowerId, 3);
        return ResponseEntity.ok(response);
    }
}
