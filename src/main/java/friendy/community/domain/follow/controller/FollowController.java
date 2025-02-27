package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follows")
public class FollowController implements SpringDocFollowController{
    private final FollowService followService;

    @PostMapping("/{targetId}")
    public ResponseEntity<Void> follow(
        HttpServletRequest httpServletRequest,
        @PathVariable final Long targetId
    ) {
        followService.follow(httpServletRequest, targetId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> unfollow(
        HttpServletRequest httpServletRequest,
        @PathVariable Long targetId
    ) {
        followService.unfollow(httpServletRequest, targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{targetId}/following")
    public ResponseEntity<FollowListResponse> getFollowingMembers(
        @PathVariable final Long targetId,
        @RequestParam(required = false) Long cursor
    ) {
        FollowListResponse response = followService.getFollowingMembers(targetId, cursor, 10);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetId}/followers")
    public ResponseEntity<FollowListResponse> getFollowerMembers(
        @PathVariable final Long targetId,
        @RequestParam(required = false) Long cursor
    ) {
        FollowListResponse response = followService.getFollowerMembers(targetId, cursor, 10);
        return ResponseEntity.ok(response);
    }
}
