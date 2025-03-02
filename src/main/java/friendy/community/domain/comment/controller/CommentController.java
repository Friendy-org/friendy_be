package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
import friendy.community.domain.comment.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController implements SpringDocCommentController {

    private final CommentService commentService;

    @PostMapping()
    public ResponseEntity<Void> createComment(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CommentCreateRequest commentRequest
    ) {
        commentService.saveComment(commentRequest, httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply")
    public ResponseEntity<Void> createReply(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody ReplyCreateRequest replyRequest
    ) {
        commentService.saveReply(replyRequest, httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            HttpServletRequest httpServletRequest,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        commentService.updateComment(commentUpdateRequest, commentId, httpServletRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reply/{replyId}")
    public ResponseEntity<Void> updateReply(
            HttpServletRequest httpServletRequest,
            @PathVariable Long replyId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        commentService.updateReply(commentUpdateRequest, replyId, httpServletRequest);
        return ResponseEntity.ok().build();
    }
}
