package friendy.community.domain.email.controller;

import friendy.community.domain.email.dto.request.EmailRequest;
import friendy.community.domain.email.dto.request.VerifyCodeRequest;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "사용자 검증 API", description = "사용자 검증 API")
public interface SpringDocEmailController {

    @Operation(summary = "이메일 인증코드 전송")
    @ApiResponse(responseCode = "200", description = "인증코드 전송 성공")
    @ApiErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, instance = "/send-code", errorCases = {
            @ErrorCase(description = "인증번호 전송 실패", exampleMessage = "이메일 전송에 실패했습니다."),
    })
    ResponseEntity<FriendyResponse<Void>> sendAuthenticatedEmail(EmailRequest request);

    @Operation(summary = "이메일 인증코드 검증")
    @ApiResponse(responseCode = "200", description = "인증코드 검증 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/verify-code", errorCases = {
            @ErrorCase(description = "인증번호 없음", exampleMessage = "인증번호가 존재하지 않습니다."),
            @ErrorCase(description = "인증번호 불일치", exampleMessage = "인증번호가 일치하지 않습니다."),
    })
    ResponseEntity<FriendyResponse<Void>> verifyAuthCode(VerifyCodeRequest request);
}
