package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.request.CommentCreateRequest;
import friendy.community.domain.comment.dto.request.CommentUpdateRequest;
import friendy.community.domain.comment.dto.response.FindAllCommentsResponse;
import friendy.community.domain.comment.dto.response.FindCommentResponse;
import friendy.community.domain.comment.dto.request.ReplyCreateRequest;
import friendy.community.domain.comment.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

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

    @GetMapping("/list")
    public ResponseEntity<FindAllCommentsResponse> getAllComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 10);
        return ResponseEntity.ok(commentService.getComments(pageable, postId));
    }
}
