package friendy.community.domain.auth.controller;

import friendy.community.domain.auth.dto.request.LoginRequest;
import friendy.community.domain.auth.dto.response.TokenResponse;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "인증 및 인가 API", description = "인증 및 인가 API")
public interface SpringDocAuthController {

    @Operation(summary = "로그인")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            headers = {
                    @Header(name = "Authorization", description = "액세스 토큰", required = true, schema = @Schema(type = "string")),
                    @Header(name = "Authorization-Refresh", description = "리프레시 토큰", required = true, schema = @Schema(type = "string"))
            }
    )
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/auth/login", errorCases = {
            @ErrorCase(description = "이메일 입력 없음", exampleMessage = "이메일이 입력되지 않았습니다."),
            @ErrorCase(description = "이메일 형식 오류", exampleMessage = "이메일 형식으로 입력해주세요."),
            @ErrorCase(description = "비밀번호 입력 없음", exampleMessage = "비밀번호가 입력되지 않았습니다."),
            @ErrorCase(description = "비밀번호 형식 오류", exampleMessage = "숫자, 영문자, 특수문자(~!@#$%^&*?)를 포함해야 합니다."),
            @ErrorCase(description = "비밀번호 글자수 오류", exampleMessage = "비밀번호는 8~16자 사이로 입력해주세요."),
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/auth/login", errorCases = {
            @ErrorCase(description = "이메일 불일치", exampleMessage = "해당 이메일의 회원이 존재하지 않습니다."),
            @ErrorCase(description = "비밀번호 불일치", exampleMessage = "로그인에 실패하였습니다. 비밀번호를 확인해주세요."),
    })
    ResponseEntity<FriendyResponse<TokenResponse>> login(LoginRequest request);

    @Operation(
            summary = "로그아웃",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            }
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/auth/logout", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "잘못된 액세스 토큰", exampleMessage = "인증 실패(잘못된 액세스 토큰) - 토큰 : {token}"),
            @ErrorCase(description = "액세스 토큰 만료", exampleMessage = "인증 실패(만료된 액세스 토큰) - 토큰 : {token}"),
            @ErrorCase(description = "액세스 토큰에 이메일 클레임 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}"),
            @ErrorCase(description = "로그아웃 상태인 사용자가 요청", exampleMessage = "로그인 되어있지 않은 사용자입니다.")
    })
    ResponseEntity<FriendyResponse<Void>> logout(HttpServletRequest httpServletRequest);

    @Operation(
            summary = "토큰 재발급",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            }
    )
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
            headers = {
                    @Header(name = "Authorization", description = "새로운 액세스 토큰", required = true, schema = @Schema(type = "string")),
                    @Header(name = "Authorization-Refresh", description = "새로운 리프레시 토큰", required = true, schema = @Schema(type = "string"))
            }
    )
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/auth/token/reissue", errorCases = {
            @ErrorCase(description = "잘못된 리프레시 토큰", exampleMessage = "인증 실패(잘못된 리프레시 토큰) - 토큰 : {token}"),
            @ErrorCase(description = "리프레시 토큰 만료", exampleMessage = "인증 실패(만료된 리프레시 토큰) - 토큰 : {token}"),
            @ErrorCase(description = "리프레시 토큰에 이메일 클레임 없음", exampleMessage = "인증 실패(JWT 리프레시 토큰 Payload 이메일 누락) - 토큰 : {token}"),
            @ErrorCase(description = "로그아웃 상태인 사용자가 요청", exampleMessage = "로그인 되어있지 않은 사용자입니다.")
    })
    ResponseEntity<FriendyResponse<TokenResponse>> reissueToken(HttpServletRequest httpServletRequest);

    @Operation(
            summary = "회원 탈퇴",
            security = {
                    @SecurityRequirement(name = "bearerAuth"),
                    @SecurityRequirement(name = "refreshAuth")
            }
    )
    @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/withdrawal", errorCases = {
            @ErrorCase(description = "잘못된 액세스 토큰", exampleMessage = "인증 실패(잘못된 액세스 토큰) - 토큰 : {token}"),
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "액세스 토큰 만료", exampleMessage = "인증 실패(만료된 액세스 토큰) - 토큰 : {token}"),
            @ErrorCase(description = "액세스 토큰에 이메일 클레임 없음", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}"),
            @ErrorCase(description = "로그아웃 상태인 사용자가 요청", exampleMessage = "로그인 되어있지 않은 사용자입니다.")
    })
    ResponseEntity<FriendyResponse<Void>> withdrawal(HttpServletRequest httpServletRequest);
}
