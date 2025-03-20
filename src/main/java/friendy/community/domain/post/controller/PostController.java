package friendy.community.domain.post.controller;

import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.service.PostService;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.security.FriendyUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController implements SpringDocPostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> createPost(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @Valid @RequestBody PostCreateRequest postCreateRequest
    ) {
        Long postId = postService.savePost(postCreateRequest, userDetails.getMemberId());
        return ResponseEntity.created(URI.create("/posts/" + postId)).build();
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long postId,
        @Valid @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        Long returnPostId = postService.updatePost(postUpdateRequest, userDetails.getMemberId(), postId);
        return ResponseEntity.created(URI.create("/posts/" + returnPostId)).build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long postId
    ) {
        postService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<FindPostResponse> getPost(
        @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping("/list")
    public ResponseEntity<FriendyResponse<FindAllPostResponse>> getAllPosts(
        @RequestParam(required = false) Long lastPostId
    ) {
        FriendyResponse<FindAllPostResponse> response = FriendyResponse.of(200, "회원가입성공",postService.getPostsByLastId(lastPostId));
        return ResponseEntity.ok(response);
    }

}