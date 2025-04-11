package friendy.community.domain.post.controller;

import friendy.community.domain.post.controller.code.PostSuccessCode;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.dto.response.PostIdResponse;
import friendy.community.domain.post.service.PostCommandService;
import friendy.community.domain.post.service.PostQueryService;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.security.annotation.LoggedInUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController implements SpringDocPostController {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    @PostMapping
    public ResponseEntity<FriendyResponse<Void>> createPost(
        @LoggedInUser FriendyUserDetails userDetails,
        @Valid @RequestBody PostCreateRequest postCreateRequest
    ) {
        postCommandService.savePost(postCreateRequest, userDetails.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(FriendyResponse.of(PostSuccessCode.CREATE_POST_SUCCESS));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<FriendyResponse<PostIdResponse>> updatePost(
        @LoggedInUser FriendyUserDetails userDetails,
        @PathVariable Long postId,
        @Valid @RequestBody PostUpdateRequest postUpdateRequest
    ) {
        return ResponseEntity.ok(FriendyResponse.of(PostSuccessCode.UPDATE_POST_SUCCESS,
            postCommandService.updatePost(postUpdateRequest, userDetails.getMemberId(), postId)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<FriendyResponse<Void>> deletePost(
        @LoggedInUser FriendyUserDetails userDetails,
        @PathVariable Long postId
    ) {
        postCommandService.deletePost(userDetails.getMemberId(), postId);
        return ResponseEntity.ok(FriendyResponse.of(PostSuccessCode.DELETE_POST_SUCCESS));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<FriendyResponse<FindPostResponse>> getPost(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long postId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(
            PostSuccessCode.GET_POST_SUCCESS,
            postQueryService.getPost(postId, userDetails.getMemberId())));
    }

    @GetMapping("/list")
    public ResponseEntity<FriendyResponse<FindAllPostResponse>> getAllPosts(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @RequestParam(required = false) Long lastPostId
    ) {
        return ResponseEntity.ok(FriendyResponse.of(
            PostSuccessCode.GET_ALL_POSTS_SUCCESS,
            postQueryService.getPostsByLastId(lastPostId, userDetails.getMemberId())));
    }
}