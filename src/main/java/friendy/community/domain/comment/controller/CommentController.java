package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.controller.code.CommentSuccessCode;
import friendy.community.domain.comment.dto.FindAllReplyResponse;
import friendy.community.domain.comment.dto.request.CommentCreateRequest;
import friendy.community.domain.comment.dto.request.CommentUpdateRequest;
import friendy.community.domain.comment.dto.response.FindAllCommentsResponse;
import friendy.community.domain.comment.dto.request.ReplyCreateRequest;
import friendy.community.domain.comment.service.CommentService;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.security.annotation.LoggedInUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController implements SpringDocCommentController {

    private final CommentService commentService;

    @PostMapping()
    public ResponseEntity<FriendyResponse<Void>> createComment(
        @LoggedInUser FriendyUserDetails userDetails,
        @Valid @RequestBody CommentCreateRequest commentRequest
    ) {
        commentService.saveComment(commentRequest, userDetails.getMemberId());
        return ResponseEntity.ok(FriendyResponse.of(CommentSuccessCode.COMMENT_CREATE_SUCCESS));
    }

    @PostMapping("/reply")
    public ResponseEntity<FriendyResponse<Void>> createReply(
        @LoggedInUser FriendyUserDetails userDetails,
        @Valid @RequestBody ReplyCreateRequest replyRequest
    ) {
        commentService.saveReply(replyRequest, userDetails.getMemberId());
        return ResponseEntity.ok(FriendyResponse.of(CommentSuccessCode.REPLY_CREATE_SUCCESS));
    }

    
    @PostMapping("/{commentId}")
    public ResponseEntity<FriendyResponse<Void>>updateComment(
        @LoggedInUser FriendyUserDetails userDetails,
        @PathVariable Long commentId,
        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        commentService.updateComment(commentUpdateRequest, commentId, userDetails.getMemberId());
        return ResponseEntity.ok(FriendyResponse.of(CommentSuccessCode.COMMENT_UPDATE_SUCCESS));
    }

    @PostMapping("/reply/{replyId}")
    public ResponseEntity<FriendyResponse<Void>> updateReply(
        @LoggedInUser FriendyUserDetails userDetails,
        @PathVariable Long replyId,
        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        commentService.updateReply(commentUpdateRequest, replyId, userDetails.getMemberId());
        return ResponseEntity.ok(FriendyResponse.of(CommentSuccessCode.REPLY_UPDATE_SUCCESS));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @LoggedInUser FriendyUserDetails userDetails,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reply/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @LoggedInUser FriendyUserDetails userDetails,
            @PathVariable Long replyId
    ) {
        commentService.deleteReply(replyId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list/{postId}")
    public ResponseEntity<FriendyResponse<FindAllCommentsResponse>> getAllComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long lastCommentId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(CommentSuccessCode.GET_ALL_COMMENTS_SUCCESS, commentService.getCommentsByLastId(lastCommentId)));
    }

    @GetMapping("/reply/list/{commentId}")
    public ResponseEntity<FriendyResponse<FindAllReplyResponse>> getAllReplies(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long lastReplyId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(CommentSuccessCode.REPLY_GET_ALL_SUCCESS, commentService.getRepliesByLastId(lastReplyId)));
    }
}
