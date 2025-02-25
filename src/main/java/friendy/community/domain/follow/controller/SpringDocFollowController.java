package friendy.community.domain.follow.controller;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "팔로워 API", description = "팔로워 API")
public interface SpringDocFollowController {
    @Operation(summary = "팔로우 신청")
    @ApiResponse(responseCode = "201", description = "팔로잉 성공")
    ResponseEntity<Void> follow(
        HttpServletRequest httpServletRequest,
        @PathVariable Long targetId
    );

}
