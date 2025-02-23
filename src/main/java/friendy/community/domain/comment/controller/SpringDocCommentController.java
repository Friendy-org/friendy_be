package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
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
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments", errorCases = {
            @ErrorCase(description = "댓글 내용 없음", exampleMessage = "댓글 내용이 입력되지 않았습니다."),
            @ErrorCase(description = "댓글 내용 길이 초과", exampleMessage = "댓글은 1100자 이내로 작성해주세요."),
            @ErrorCase(description = "댓글 작성 대상 게시글 누락", exampleMessage = "댓글이 달릴 게시글이 명시되지 않았습니다."),
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "JWT 액세스 토큰 Payload 이메일 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}"),
            @ErrorCase(description = "조작된 액세스 토큰", exampleMessage = "해당 이메일의 회원이 존재하지 않습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments", errorCases = {
            @ErrorCase(description = "잘못된 게시글 id", exampleMessage = "댓글 작성 대상 게시글이 존재하지 않습니다.")
    })
    ResponseEntity<Void> createComment(
            HttpServletRequest httpServletRequest,
            @RequestBody CommentCreateRequest commentRequest
    );

    @Operation(summary = "답글 작성")
    @ApiResponse(responseCode = "201", description = "답글 작성 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments/reply", errorCases = {
            @ErrorCase(description = "답글 내용 없음", exampleMessage = "답글 내용이 입력되지 않았습니다."),
            @ErrorCase(description = "답글 내용 길이 초과", exampleMessage = "답글은 1100자 이내로 작성해주세요."),
            @ErrorCase(description = "답글 작성 대상 게시글 누락", exampleMessage = "답글이 달릴 게시글이 명시되지 않았습니다."),
            @ErrorCase(description = "답글 작성 대상 댓글 누락", exampleMessage = "답글이 달릴 댓글이 명시되지 않았습니다."),
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "JWT 액세스 토큰 Payload 이메일 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}"),
            @ErrorCase(description = "조작된 액세스 토큰", exampleMessage = "해당 이메일의 회원이 존재하지 않습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments", errorCases = {
            @ErrorCase(description = "잘못된 게시글 id", exampleMessage = "댓글 작성 대상 게시글이 존재하지 않습니다."),
            @ErrorCase(description = "잘못된 댓글 id", exampleMessage = "존재하지 않는 댓글입니다.")
    })
    ResponseEntity<Void> createReply(
            HttpServletRequest httpServletRequest,
            @RequestBody ReplyCreateRequest replyRequest
    );
}
