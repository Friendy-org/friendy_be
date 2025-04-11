package friendy.community.domain.follow.controller;

import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.follow.dto.response.FollowListResponse;
import friendy.community.domain.follow.service.FollowCommandService;
import friendy.community.domain.follow.service.FollowQueryService;
import friendy.community.global.config.MockSecurityConfig;
import friendy.community.global.config.SecurityConfig;
import friendy.community.global.config.WebConfig;
import friendy.community.global.security.FriendyUserDetails;
import friendy.community.global.security.resolver.LoggedInUserArgumentResolver;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LoggedInUserArgumentResolver.class)
    })
@Import(MockSecurityConfig.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowCommandService followCommandService;

    @MockitoBean
    private FollowQueryService followQueryService;

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
        // given
        doNothing().when(followCommandService).follow(any(), any());

        // when & then
        mockMvc.perform(post("/follow/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("언팔로우 API 성공")
    void unfollowSuccess() throws Exception {
        // given
        doNothing().when(followCommandService).unfollow(any(), any());

        // when & then
        mockMvc.perform(delete("/follow/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("팔로잉 목록 조회 API 성공")
    void getFollowingMembersSuccess() throws Exception {
        // given
        FollowListResponse mockResponse = new FollowListResponse(Collections.emptyList(), false, null);
        when(followQueryService.getFollowingList(any(), any()))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/follow/following/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("팔로워 목록 조회 API 성공")
    void getFollowerMembersSuccess() throws Exception {
        // given
        FollowListResponse mockResponse = new FollowListResponse(Collections.emptyList(), false, null);
        when(followQueryService.getFollowerList(any(), any()))
            .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/follow/follower/1")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
