package friendy.community.domain.member.controller;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.service.MemberService;
import friendy.community.global.response.FriendyResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController implements SpringDocMemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<FriendyResponse<Void>> signUp(@Valid @RequestBody MemberSignUpRequest request) {
        memberService.signUp(request);
        return ResponseEntity.ok(FriendyResponse.of(201, "회원가입성공"));
    }

    @PostMapping("/password")
    public ResponseEntity<FriendyResponse<Void>> password(
        @Valid @RequestBody PasswordRequest passwordRequest
    ) {
        memberService.resetPassword(passwordRequest);
        return ResponseEntity.ok(FriendyResponse.of(200, "비밀번호 변경 성공"));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<FriendyResponse<FindMemberResponse>> findMember(
        HttpServletRequest httpServletRequest,
        @PathVariable Long memberId
    ) {
        memberService.getMember(httpServletRequest, memberId);
        FriendyResponse<FindMemberResponse> response = FriendyResponse.of(200,
            "프로필 조회 성공",
            memberService.getMember(httpServletRequest, memberId));
        return ResponseEntity.ok(response);
    }
}
