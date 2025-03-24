package friendy.community.domain.email.controller;

import friendy.community.domain.email.controller.code.EmailSuccessCode;
import friendy.community.domain.email.dto.request.EmailRequest;
import friendy.community.domain.email.dto.request.VerifyCodeRequest;
import friendy.community.domain.email.service.EmailService;
import friendy.community.global.response.FriendyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController implements SpringDocEmailController {

    private final EmailService emailService;

    @PostMapping("/send-code")
    public ResponseEntity<FriendyResponse<Void>> sendAuthenticatedEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendAuthenticatedEmail(request);
        return ResponseEntity.ok(FriendyResponse.of(EmailSuccessCode.AUTH_CODE_SENT));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<FriendyResponse<Void>> verifyAuthCode(@Valid @RequestBody VerifyCodeRequest request) {
        emailService.verifyAuthCode(request);
        return ResponseEntity.ok(FriendyResponse.of(EmailSuccessCode.AUTH_CODE_VERIFIED));
    }
}
