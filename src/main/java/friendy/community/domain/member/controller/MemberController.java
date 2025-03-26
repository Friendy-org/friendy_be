package friendy.community.domain.member.controller;

import friendy.community.domain.member.controller.code.MemberSuccessCode;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.service.MemberService;
import friendy.community.global.response.FriendyResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController implements SpringDocMemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<FriendyResponse<Void>> signup(@Valid @RequestBody MemberSignUpRequest request) {
        memberService.signup(request);
        FriendyResponse<Void> response = FriendyResponse.of(MemberSuccessCode.SIGN_UP_SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/password")
    public ResponseEntity<FriendyResponse<Void>> changePassword(
        @Valid @RequestBody PasswordRequest passwordRequest
    ) {
        memberService.changePassword(passwordRequest);
        return ResponseEntity.ok(FriendyResponse.of(MemberSuccessCode.CHANGE_PASSWORD_SUCCESS));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<FriendyResponse<FindMemberResponse>> getMemberInfo(
        HttpServletRequest httpServletRequest,
        @PathVariable Long memberId
    ) {
        memberService.getMemberInfo(httpServletRequest, memberId);
        FriendyResponse<FindMemberResponse> response = FriendyResponse.of(MemberSuccessCode.GET_MEMBER_INFO_SUCCESS, memberService.getMemberInfo(httpServletRequest, memberId));
        return ResponseEntity.ok(response);
    }
}
