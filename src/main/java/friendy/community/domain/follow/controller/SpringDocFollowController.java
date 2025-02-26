package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "팔로워 API", description = "팔로워 API")
public interface SpringDocFollowController {
    @Operation(summary = "팔로우 신청")
    @ApiResponse(responseCode = "201", description = "팔로잉 성공")
    ResponseEntity<Void> follow(
        HttpServletRequest httpServletRequest,
        @PathVariable Long targetId
    );

    @Operation(summary = "팔로잉 리스트 조회", description = "특정 사용자가 팔로우하는 멤버 목록을 가져옵니다. 무한 스크롤을 위해 cursor를 사용할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공")
    public ResponseEntity<FollowListResponse> getFollowingMembers(
        @PathVariable Long memberId,
        @RequestParam(required = false) Long cursor
    );
}
