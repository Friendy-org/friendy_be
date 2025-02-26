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
        @PathVariable Long targetId
    ) {
        followService.follow(httpServletRequest, targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{memberId}/following")
    public ResponseEntity<FollowListResponse> getFollowingMembers(
        @PathVariable Long memberId,
        @RequestParam(required = false) Long cursor
    ) {
        FollowListResponse response = followService.getFollowingMembers(memberId, cursor, 10);
        return ResponseEntity.ok(response); // ResponseEntity로 감싸서 반환
    }
}
