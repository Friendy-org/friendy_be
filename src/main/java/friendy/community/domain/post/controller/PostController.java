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
    public ResponseEntity<FriendyResponse<Void>> createPost(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest postCreateRequest
    ) {
        Long postId = postService.savePost(postCreateRequest, userDetails.getMemberId());
        return ResponseEntity.ok(FriendyResponse.of(201, "게시글 생성 성공"));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<FriendyResponse<Void>> updatePost(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        Long returnPostId = postService.updatePost(postUpdateRequest, userDetails.getMemberId(), postId);
        return ResponseEntity.ok(FriendyResponse.of(201, "게시글 수정 성공"));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<FriendyResponse<Void>> deletePost(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(FriendyResponse.of(200, "게시글 삭제 성공"));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<FriendyResponse<FindPostResponse>> getPost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(200, "게시글 조회 성공", postService.getPost(postId)));
    }

    @GetMapping("/list")
    public ResponseEntity<FriendyResponse<FindAllPostResponse>> getAllPosts(
            @RequestParam(required = false) Long lastPostId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(200, "게시글 목록 조회 성공", postService.getPostsByLastId(lastPostId)));
    }
}