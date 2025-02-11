package friendy.community.domain.comment.controller;

import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController implements SpringDocCommentController {

    private final CommentService commentService;

    @PostMapping("/write")
    public ResponseEntity<Void> createComment(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CommentCreateRequest commentRequest
    ) {
        Long commentId = commentService.saveComment(commentRequest, httpServletRequest);
        return ResponseEntity.created(URI.create("/comments/" + commentId)).build();
    }
}
