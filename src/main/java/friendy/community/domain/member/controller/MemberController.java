package friendy.community.domain.member.controller;

import friendy.community.domain.member.controller.code.MemberSuccessCode;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberQueryService;
import friendy.community.domain.post.controller.code.PostSuccessCode;
import friendy.community.domain.member.dto.response.FindMemberPostsResponse;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.security.FriendyUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController implements SpringDocMemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @PostMapping("/signup")
    public ResponseEntity<FriendyResponse<Void>> signup(@Valid @RequestBody MemberSignUpRequest request) {
        memberCommandService.signup(request);
        FriendyResponse<Void> response = FriendyResponse.of(MemberSuccessCode.SIGN_UP_SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/password")
    public ResponseEntity<FriendyResponse<Void>> changePassword(
        @Valid @RequestBody PasswordRequest passwordRequest
    ) {
        memberCommandService.changePassword(passwordRequest);
        return ResponseEntity.ok(FriendyResponse.of(MemberSuccessCode.CHANGE_PASSWORD_SUCCESS));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<FriendyResponse<FindMemberResponse>> getMemberInfo(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long memberId
    ) {
        FriendyResponse<FindMemberResponse> response = FriendyResponse.of(
            MemberSuccessCode.GET_MEMBER_INFO_SUCCESS,
            memberQueryService.getMemberInfo(userDetails.getMemberId(), memberId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}/posts")
    public ResponseEntity<FriendyResponse<FindMemberPostsResponse>> getMemberPosts(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long memberId,
        @RequestParam(required = false) Long lastPostId
    ) {
        FindMemberPostsResponse response = memberQueryService.getMemberPosts(memberId, lastPostId);
        return ResponseEntity.ok(
            FriendyResponse.of(PostSuccessCode.GET_MEMBER_POSTS_SUCCESS, response)
        );
    }
}

