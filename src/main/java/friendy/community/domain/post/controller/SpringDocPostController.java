package friendy.community.domain.post.controller;

import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "게시글 API", description = "게시글 생성 API")
public interface SpringDocPostController {

    @Operation(summary = "게시글 생성", description = "새 게시글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "게시글 생성 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/posts", errorCases = {
            @ErrorCase(description = "게시글 내용 없음", exampleMessage = "게시글 내용이 입력되지 않았습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/posts", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "JWT 액세스 토큰 Payload 이메일 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}")

    })
    ResponseEntity<Void> createPost(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @RequestBody PostCreateRequest postRequest
    );

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "게시글 내용 없음", exampleMessage = "게시글 내용이 입력되지 않았습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "존재하지 않는 게시글 ID", exampleMessage = "존재하지 않는 게시글입니다.")
    })
    @ApiErrorResponse(status = HttpStatus.FORBIDDEN, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "작성자가 아닌 사용자가 수정 시도", exampleMessage = "게시글은 작성자 본인만 관리할 수 있습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "JWT 액세스 토큰 Payload 이메일 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}")
    })
    @ApiErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, instance = "/posts/{postId}", errorCases = {
        @ErrorCase(description = "파일 삭제 실패", exampleMessage = "파일을 삭제하지 못햇습니다.")
    })
    ResponseEntity<Void> updatePost(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest postUpdateRequest
    );

    @Operation(summary = "게시글 삭제", description = "기존 게시글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 삭제 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "존재하지 않는 게시글 ID", exampleMessage = "존재하지 않는 게시글입니다.")
    })
    @ApiErrorResponse(status = HttpStatus.FORBIDDEN, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "작성자가 아닌 사용자가 삭제 시도", exampleMessage = "게시글은 작성자 본인만 관리할 수 있습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.UNAUTHORIZED, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "액세스 토큰 추출 실패", exampleMessage = "인증 실패(액세스 토큰 추출 실패) - 토큰 : {token}"),
            @ErrorCase(description = "JWT 액세스 토큰 Payload 이메일 누락", exampleMessage = "인증 실패(JWT 액세스 토큰 Payload 이메일 누락) - 토큰 : {token}")

    })
    @ApiErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, instance = "/posts/{postId}", errorCases = {
        @ErrorCase(description = "파일 삭제 실패", exampleMessage = "파일을 삭제하지 못햇습니다.")
    })
    ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal FriendyUserDetails userDetails,
            @PathVariable Long postId
    );

    @Operation(summary = "게시글 조회", description = "페이지네이션을 통해 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 조회 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/posts/{postId}", errorCases = {
            @ErrorCase(description = "존재하지 않는 게시글 ID", exampleMessage = "존재하지 않는 게시글입니다.")
    })
    ResponseEntity<FindPostResponse> getPost(
            @PathVariable Long postId
    );

    @Operation(summary = "게시글 목록 조회", description = "페이지네이션을 통해 게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/posts/list", errorCases = {
        @ErrorCase(description = "게시글이 없습니다.", exampleMessage = "게시글이 없습니다.")
    })
    ResponseEntity<FindAllPostResponse> getAllPosts(
        @RequestParam(required = false) Long lastPostId
    );

}