package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.FindAllReplyResponse;
import friendy.community.domain.comment.dto.response.FindAllCommentsResponse;
import friendy.community.global.response.FriendyResponse;
import friendy.community.domain.comment.dto.request.CommentCreateRequest;
import friendy.community.domain.comment.dto.request.CommentUpdateRequest;
import friendy.community.domain.comment.dto.request.ReplyCreateRequest;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.domain.comment.dto.response.FindAllCommentsResponse;
import friendy.community.global.security.annotation.LoggedInUser;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "댓글 API", description = "댓글 API")
public interface SpringDocCommentController {
    @Operation(summary = "댓글 작성")
    @ApiResponse(responseCode = "200", description = "댓글 작성 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments", errorCases = {
            @ErrorCase(description = "댓글 내용 없음", exampleMessage = "댓글 내용이 입력되지 않았습니다."),
            @ErrorCase(description = "댓글 내용 길이 초과", exampleMessage = "댓글은 1100자 이내로 작성해주세요."),
            @ErrorCase(description = "댓글 작성 대상 게시글 누락", exampleMessage = "댓글이 달릴 게시글이 명시되지 않았습니다."),
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments", errorCases = {
            @ErrorCase(description = "잘못된 게시글 id", exampleMessage = "요청 게시글이 존재하지 않습니다.")
    })
    ResponseEntity<FriendyResponse<Void>> createComment(
            @LoggedInUser FriendyUserDetails userDetails,
            @RequestBody CommentCreateRequest commentRequest
    );

    @Operation(summary = "댓글 수정")
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments/{commentId}", errorCases = {
            @ErrorCase(description = "댓글 내용 없음", exampleMessage = "댓글 내용이 입력되지 않았습니다."),
            @ErrorCase(description = "댓글 내용 길이 초과", exampleMessage = "댓글은 1100자 이내로 작성해주세요.")
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments/{commentId}", errorCases = {
            @ErrorCase(description = "다른 사용자의 댓글 수정 시도", exampleMessage = "작성자만 댓글을 수정할 수 있습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/{commentId}", errorCases = {
            @ErrorCase(description = "잘못된 댓글 id", exampleMessage = "존재하지 않는 댓글입니다.")
    })
    ResponseEntity<FriendyResponse<Void>> updateComment(
            @LoggedInUser FriendyUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest commentUpdateRequest
    );

    @Operation(summary = "답글 작성")
    @ApiResponse(responseCode = "200", description = "답글 작성 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments/reply", errorCases = {
            @ErrorCase(description = "답글 내용 없음", exampleMessage = "답글 내용이 입력되지 않았습니다."),
            @ErrorCase(description = "답글 내용 길이 초과", exampleMessage = "답글은 1100자 이내로 작성해주세요."),
            @ErrorCase(description = "답글 작성 대상 게시글 누락", exampleMessage = "답글이 달릴 게시글이 명시되지 않았습니다."),
            @ErrorCase(description = "답글 작성 대상 댓글 누락", exampleMessage = "답글이 달릴 댓글이 명시되지 않았습니다."),
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/reply", errorCases = {
            @ErrorCase(description = "잘못된 게시글 id", exampleMessage = "요청 게시글이 존재하지 않습니다."),
            @ErrorCase(description = "잘못된 댓글 id", exampleMessage = "존재하지 않는 댓글입니다.")
    })
    ResponseEntity<FriendyResponse<Void>> createReply(
            @LoggedInUser FriendyUserDetails userDetails,
            @RequestBody ReplyCreateRequest replyRequest
    );

    @Operation(summary = "답글 수정")
    @ApiResponse(responseCode = "200", description = "답글 수정 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/comments/reply/{commentId}", errorCases = {
            @ErrorCase(description = "답글 내용 없음", exampleMessage = "답글 내용이 입력되지 않았습니다."),
            @ErrorCase(description = "답글 내용 길이 초과", exampleMessage = "답글은 1100자 이내로 작성해주세요.")
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments/reply/{commentId}", errorCases = {
            @ErrorCase(description = "다른 사용자의 답글 수정 시도", exampleMessage = "작성자만 답글을 수정할 수 있습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/reply/{commentId}", errorCases = {
            @ErrorCase(description = "잘못된 답글 id", exampleMessage = "존재하지 않는 답글입니다.")
    })
    ResponseEntity<FriendyResponse<Void>> updateReply(
            @LoggedInUser FriendyUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest commentUpdateRequest
    );

    @Operation(summary = "댓글 삭제")
    @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments/{commentId}", errorCases = {
            @ErrorCase(description = "다른 사용자의 댓글 삭제 시도", exampleMessage = "작성자만 댓글을 수정할 수 있습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/{commentId}", errorCases = {
            @ErrorCase(description = "잘못된 댓글 id", exampleMessage = "존재하지 않는 댓글입니다.")
    })
    ResponseEntity<Void> deleteComment(
            @LoggedInUser FriendyUserDetails userDetails,
            @PathVariable Long commentId
    );

    @Operation(summary = "답글 삭제")
    @ApiResponse(responseCode = "200", description = "답글 삭제 성공")
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/comments/reply/{replyId}", errorCases = {
            @ErrorCase(description = "다른 사용자의 답글 삭제 시도", exampleMessage = "작성자만 답글을 수정할 수 있습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/reply/{replyId}", errorCases = {
            @ErrorCase(description = "잘못된 답글 id", exampleMessage = "존재하지 않는 답글입니다.")
    })
    ResponseEntity<Void> deleteReply(
            @LoggedInUser FriendyUserDetails userDetails,
            @PathVariable Long replyId
    );

    @Operation(summary = "댓글 조회")
    @ApiResponse(responseCode = "200", description = "댓글 조회 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/list", errorCases = {
            @ErrorCase(description = "잘못된 게시글 id", exampleMessage = "요청 게시글이 존재하지 않습니다."),
            @ErrorCase(description = "페이지 범위 초과", exampleMessage = "요청한 페이지가 존재하지 않습니다.")
    })
    ResponseEntity<FriendyResponse<FindAllCommentsResponse>> getAllComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long lastCommentId
    );

    @Operation(summary = "답글 조회")
    @ApiResponse(responseCode = "200", description = "답글 조회 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/comments/reply/list/{replyId}", errorCases = {
            @ErrorCase(description = "잘못된 댓글 id", exampleMessage = "존재하지 않는 댓글입니다.")
    })
    ResponseEntity<FriendyResponse<FindAllReplyResponse>> getAllReplies(
            @PathVariable Long commentId,
            @RequestParam Long lastReplyId
    );
}
