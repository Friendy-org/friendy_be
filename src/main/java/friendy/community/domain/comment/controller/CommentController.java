package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
import friendy.community.domain.comment.service.CommentService;
import friendy.community.global.security.FriendyUserDetails;
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
    public ResponseEntity<Void> createComment(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @Valid @RequestBody CommentCreateRequest commentRequest
    ) {
        commentService.saveComment(commentRequest, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply")
    public ResponseEntity<Void> createReply(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @Valid @RequestBody ReplyCreateRequest replyRequest
    ) {
        commentService.saveReply(replyRequest, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long commentId,
        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        commentService.updateComment(commentUpdateRequest, commentId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply/{replyId}")
    public ResponseEntity<Void> updateReply(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long replyId,
        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        commentService.updateReply(commentUpdateRequest, replyId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }
}
