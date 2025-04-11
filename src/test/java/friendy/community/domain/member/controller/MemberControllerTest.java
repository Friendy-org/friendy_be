package friendy.community.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.dto.request.PasswordRequest;
import friendy.community.domain.member.dto.response.FindMemberPostsResponse;
import friendy.community.domain.member.dto.response.FindMemberResponse;
import friendy.community.domain.member.dto.response.PostPreview;
import friendy.community.domain.member.service.MemberCommandService;
import friendy.community.domain.member.service.MemberQueryService;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LoggedInUserArgumentResolver.class)
    })
@Import(MockSecurityConfig.class)
class MemberControllerTest {

    FriendyUserDetails userDetails;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberCommandService memberCommandService;

    @MockitoBean
    private MemberQueryService memberQueryService;

    @BeforeEach
    void setup() {
        userDetails = new FriendyUserDetails(1L, "example@friendy.com", "password", Collections.emptyList());
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("회원가입 성공 시 201 반환")
    void signupSuccess() throws Exception {
        // given
        MemberSignUpRequest request = new MemberSignUpRequest(
            "user@example.com",
            "nickname",
            "Password123!",
            LocalDate.of(2000, 1, 1),
            "https://example.com/image.jpg"
        );
        given(memberCommandService.signup(any())).willReturn(1L);

        // when & then
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("비밀번호 변경 성공 시 200 반환")
    void changePasswordSuccess() throws Exception {
        // given
        PasswordRequest request = new PasswordRequest(
            "test@friendy.com",
            "newPassword123!"
        );
        willDoNothing().given(memberCommandService).changePassword(any());

        // when & then
        mockMvc.perform(post("/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 멤버 정보 조회 성공 시 200 반환")
    void getMemberInfoSuccess() throws Exception {
        // given
        FindMemberResponse mockResponse = new FindMemberResponse(
            true,
            "example@friendy.com",
            "복성김",
            "https://cdn.friendy.com/images/profile.png",
            1245,
            350
        );
        given(memberQueryService.getMemberInfo(anyLong(), anyLong())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/member/{memberId}", 1L)
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.nickname").value("복성김"))
            .andExpect(jsonPath("$.result.email").value("example@friendy.com"))
            .andExpect(jsonPath("$.result.profileImageUrl").value("https://cdn.friendy.com/images/profile.png"));
    }

    @Test
    @DisplayName("특정 멤버 게시글 조회 성공 시 200 반환")
    void getMemberPostsSuccess() throws Exception {
        // given
        Long memberId = 2L;
        Long lastPostId = null;

        List<PostPreview> postPreviews = List.of(
            new PostPreview(100L, "https://example.com/image1.jpg"),
            new PostPreview(99L, "https://example.com/image2.jpg")
        );
        FindMemberPostsResponse mockResponse = new FindMemberPostsResponse(
            postPreviews,
            true,
            99L
        );
        given(memberQueryService.getMemberPosts(anyLong(), any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/member/{memberId}/posts", memberId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.posts").isArray())
            .andExpect(jsonPath("$.result.posts[0].id").value(100L))
            .andExpect(jsonPath("$.result.posts[0].thumbnail").value("https://example.com/image1.jpg"))
            .andExpect(jsonPath("$.result.hasNext").value(true))
            .andExpect(jsonPath("$.result.lastPostId").value(99L));
    }
}
