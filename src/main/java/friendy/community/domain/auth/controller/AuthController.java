package friendy.community.domain.auth.controller;

import friendy.community.domain.auth.controller.code.AuthSuccessCode;
import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.dto.response.TokenResponse;
import friendy.community.domain.auth.jwt.JwtTokenExtractor;
import friendy.community.domain.auth.service.AuthService;
import friendy.community.global.response.FriendyResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements SpringDocAuthController {

    private final AuthService authService;
    private final JwtTokenExtractor jwtTokenExtractor;

    @PostMapping("/login")
    public ResponseEntity<FriendyResponse<TokenResponse>> login(
        @Valid @RequestBody LoginRequest loginRequest
    ) {
        final TokenResponse tokenresponse = authService.login(loginRequest);

        FriendyResponse<TokenResponse> response = FriendyResponse.of(AuthSuccessCode.LOGIN_SUCCESS, tokenresponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<FriendyResponse<Void>> logout(
        HttpServletRequest httpServletRequest
    ) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        authService.logout(accessToken);

        FriendyResponse<Void> response = FriendyResponse.of(AuthSuccessCode.LOGOUT_SUCCESS);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/token/reissue")
    public ResponseEntity<FriendyResponse<TokenResponse>> reissueToken(
        HttpServletRequest httpServletRequest
    ) {
        final String refreshToken = jwtTokenExtractor.extractRefreshToken(httpServletRequest);
        final TokenResponse tokenresponse = authService.reissueToken(refreshToken);

        FriendyResponse<TokenResponse> response = FriendyResponse.of(AuthSuccessCode.TOKEN_REISSUE_SUCCESS, tokenresponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<FriendyResponse<Void>> withdrawal(
        HttpServletRequest httpServletRequest
    ) {
        final String accessToken = jwtTokenExtractor.extractAccessToken(httpServletRequest);
        authService.withdrawal(accessToken);

        FriendyResponse<Void> response = FriendyResponse.of(AuthSuccessCode.MEMBER_WITHDRAWAL_SUCCESS);
        return ResponseEntity.ok(response);
    }
}
