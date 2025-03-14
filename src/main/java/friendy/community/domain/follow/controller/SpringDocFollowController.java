package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "팔로워 API", description = "팔로워 API")
public interface SpringDocFollowController {
    @Operation(summary = "팔로우 신청")
    @ApiResponse(responseCode = "201", description = "팔로잉 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "자기 자신을 대상으로 수행할 수 없습니다.", exampleMessage = "자기 자신을 대상으로 수행할 수 없습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "해당 ID의 회원을 찾을 수 없습니다.", exampleMessage = "해당 ID의 회원을 찾을 수 없습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.CONFLICT, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "이미 팔로우한 회원", exampleMessage = "이미 팔로우한 회원입니다.")
    })
    ResponseEntity<Void> follow(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long targetId
    );

    @Operation(summary = "언팔로우", description = "특정 사용자의 팔로우를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "언팔로우 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "자기 자신을 대상으로 수행할 수 없습니다.", exampleMessage = "자기 자신을 대상으로 수행할 수 없습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "해당 ID의 회원을 찾을 수 없습니다.", exampleMessage = "해당 ID의 회원을 찾을 수 없습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.CONFLICT, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "팔로우하지 않은 회원입니다.", exampleMessage = "팔로우하지 않은 회원입니다.")
    })
    ResponseEntity<Void> unfollow(
        @AuthenticationPrincipal FriendyUserDetails userDetails,
        @PathVariable Long targetId
    );

    @Operation(summary = "팔로잉 리스트 조회", description = "특정 사용자가 팔로우하는 멤버 목록을 가져옵니다.")
    @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "해당 ID의 회원을 찾을 수 없습니다.", exampleMessage = "해당 ID의 회원을 찾을 수 없습니다.")
    })
    public ResponseEntity<FollowListResponse> getFollowingMembers(
        @PathVariable Long targetId,
        @RequestParam(required = false) Long startIndex
    );

    @Operation(summary = "팔로워 리스트 조회", description = "특정 사용자를 팔로우하는 멤버 목록을 가져옵니다.")
    @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공")
    @ApiErrorResponse(status = HttpStatus.NOT_FOUND, instance = "/follow/{targetId}", errorCases = {
        @ErrorCase(description = "해당 ID의 회원을 찾을 수 없습니다.", exampleMessage = "해당 ID의 회원을 찾을 수 없습니다.")
    })
    ResponseEntity<FollowListResponse> getFollowerMembers(
        @PathVariable Long targetId,
        @RequestParam(required = false) Long startIndex
    );
}
