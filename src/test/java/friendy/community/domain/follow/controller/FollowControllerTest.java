package friendy.community.domain.follow.controller;

import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowService;
import friendy.community.domain.post.controller.PostController;
import friendy.community.global.config.MockSecurityConfig;
import friendy.community.global.config.SecurityConfig;
import friendy.community.global.security.FriendyUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = FollowController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)
    })
@Import(MockSecurityConfig.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowService followService;

    @BeforeEach
    void setUp() {
        FriendyUserDetails userDetails = new FriendyUserDetails(
            1L,
            "user@example.com",
            "password123",
            Collections.emptyList()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("팔로우 API 성공")
    void followApiSuccess() throws Exception {
        doNothing().when(followService).follow(any(), any());

        mockMvc.perform(post("/follow/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("언팔로우 API 성공")
    void unfollowSuccess() throws Exception {
        doNothing().when(followService).unfollow(any(), any());

        mockMvc.perform(delete("/follow/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("팔로잉 목록 조회 API 성공")
    void getFollowingMembersSuccess() throws Exception {
        FollowListResponse mockResponse = new FollowListResponse(Collections.emptyList(), false);

        when(followService.getFollowingMembers(any(), any(), anyInt()))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/follow/following/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.members").isArray())
            .andExpect(jsonPath("$.members.length()").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("팔로워 목록 조회 API 성공")
    void getFollowerMembersSuccess() throws Exception {
        FollowListResponse mockResponse = new FollowListResponse(Collections.emptyList(), false);

        when(followService.getFollowerMembers(any(), any(), anyInt()))
            .thenReturn(mockResponse);

        mockMvc.perform(get("/follow/follower/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.members").isArray())
            .andExpect(jsonPath("$.members.length()").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));
    }
}
