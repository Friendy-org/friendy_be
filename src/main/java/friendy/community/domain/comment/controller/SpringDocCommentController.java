package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "댓글 API", description = "댓글 API")
public interface SpringDocCommentController {
    @Operation(summary = "댓글 작성")
    @ApiResponse(responseCode = "201", description = "댓글 작성 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments/write", errorCases = {
            @ErrorCase(description = "댓글 내용 없음", exampleMessage = "댓글 내용이 입력되지 않았습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments/write", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "JWT 액세스 토큰 Payload 이메일 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}")
    })
    ResponseEntity<Void> createComment(
            HttpServletRequest httpServletRequest,
            @RequestBody CommentCreateRequest commentRequest
    );
}
