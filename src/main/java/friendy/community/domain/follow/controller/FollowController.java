package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.controller.code.FollowSuccessCode;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowCommandService;
import friendy.community.domain.follow.service.FollowQueryService;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.security.annotation.LoggedInUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController implements SpringDocFollowController {

    private final FollowCommandService followCommandService;
    private final FollowQueryService followQueryService;

    @PostMapping("/{targetId}")
    public ResponseEntity<FriendyResponse<Void>> follow(
        @LoggedInUser FriendyUserDetails userDetails,
        @PathVariable final Long targetId
    ) {
        followCommandService.follow(userDetails.getMemberId(), targetId);
        return ResponseEntity.ok(FriendyResponse.of(FollowSuccessCode.FOLLOW_SUCCESS));
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<FriendyResponse<Void>> unfollow(
        @LoggedInUser FriendyUserDetails userDetails,
        @PathVariable Long targetId
    ) {
        followCommandService.unfollow(userDetails.getMemberId(), targetId);
        return ResponseEntity.ok(FriendyResponse.of(FollowSuccessCode.UNFOLLOW_SUCCESS));
    }

    @GetMapping("/following/{targetId}")
    public ResponseEntity<FriendyResponse<FollowListResponse>> getFollowingMembers(
        @PathVariable final Long targetId,
        @RequestParam(required = false) Long lastFollowingId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(FollowSuccessCode.GET_FOLLOWING_LIST_SUCCESS,
            followQueryService.getFollowingList(targetId, lastFollowingId)));
    }

    @GetMapping("/follower/{targetId}")
    public ResponseEntity<FriendyResponse<FollowListResponse>> getFollowerMembers(
        @PathVariable final Long targetId,
        @RequestParam(required = false) Long lastFollowerId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(FollowSuccessCode.GET_FOLLOWER_LIST_SUCCESS,
            followQueryService.getFollowerList(targetId, lastFollowerId)));
    }
}
