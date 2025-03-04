package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController implements SpringDocFollowController {
    private final FollowService followService;

    @PostMapping("/{memberId}")
    public ResponseEntity<Void> follow(
        HttpServletRequest httpServletRequest,
        @PathVariable final Long memberId
    ) {
        followService.follow(httpServletRequest, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> unfollow(
        HttpServletRequest httpServletRequest,
        @PathVariable Long memberId
    ) {
        followService.unfollow(httpServletRequest, memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/following/{memberId}")
    public ResponseEntity<FollowListResponse> getFollowingMembers(
        @PathVariable final Long memberId,
        @RequestParam(required = false) Long startIndex
    ) {
        FollowListResponse response = followService.getFollowingMembers(memberId, startIndex, 10);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/follower/{memberId}")
    public ResponseEntity<FollowListResponse> getFollowerMembers(
        @PathVariable final Long memberId,
        @RequestParam(required = false) Long startIndex
    ) {
        FollowListResponse response = followService.getFollowerMembers(memberId, startIndex, 10);
        return ResponseEntity.ok(response);
    }
}
