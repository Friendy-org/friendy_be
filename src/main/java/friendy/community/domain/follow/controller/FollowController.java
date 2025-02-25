package friendy.community.domain.follow.controller;

import friendy.community.domain.follow.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follows")
public class FollowController implements SpringDocFollowController{
    private final FollowService followService;

    @PostMapping("/{targetId}")
    public ResponseEntity<Void> follow(
        HttpServletRequest httpServletRequest,
        @PathVariable Long targetId
    ) {
        followService.follow(httpServletRequest, targetId);
        return ResponseEntity.ok().build();
    }
}
