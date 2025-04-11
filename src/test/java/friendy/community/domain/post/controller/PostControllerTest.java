package friendy.community.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindMemberResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.service.PostCommandService;
import friendy.community.domain.post.service.PostQueryService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PostController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = LoggedInUserArgumentResolver.class)
    })
@Import(MockSecurityConfig.class)
class PostControllerTest {

    private static final String BASE_URL = "/posts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostCommandService postCommandService;

    @MockitoBean
    private PostQueryService postQueryService;

    private String generateLongContent(int length) {
        return "a".repeat(length);
    }

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
    @DisplayName("게시글 생성 성공 시 201 Created 응답")
    void createPostSuccessfullyReturns201Created() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(
            "this is new content",
            List.of("프렌디", "개발", "스터디"),
            List.of("https://example.com/image.jpg"),
            "창원시"
        );
        when(postCommandService.savePost(any(PostCreateRequest.class), anyLong())).thenReturn(1L);

        // when & then
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("게시글 수정 성공 시 200 OK 응답")
    void updatePostSuccessfullyReturns200Ok() throws Exception {
        // given
        Long postId = 1L;
        PostUpdateRequest request = new PostUpdateRequest(
            "this is updated content",
            List.of("프렌디", "개발", "스터디"),
            null,
            "창원시"
        );

        // when & then
        mockMvc.perform(post(BASE_URL + "/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 성공 시 200 OK 응답")
    void deletePostSuccessfullyReturns200Ok() throws Exception {
        // given
        Long postId = 1L;
        doNothing().when(postCommandService).deletePost(anyLong(), eq(postId));

        // when & then
        mockMvc.perform(delete(BASE_URL + "/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 단건 조회 성공 시 200 OK 응답")
    void getPostSuccessfullyReturns200Ok() throws Exception {
        // given
        Long postId = 1L;
        FindPostResponse response = new FindPostResponse(
            postId,
            "this is content",
            "창원시",
            "2024-01-01T00:00:00",
            0,
            0,
            0,
            new FindMemberResponse(1L, "nickname", null),
            null,
            null
        );
        when(postQueryService.getPost(eq(postId), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get(BASE_URL + "/{postId}", postId))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 전체 목록 조회 성공 시 200 OK 응답")
    void getAllPostsSuccessfullyReturns200Ok() throws Exception {
        // given
        FindAllPostResponse response = new FindAllPostResponse(List.of(), false, null);
        when(postQueryService.getPostsByLastId(any(), anyLong())).thenReturn(response);

        // when & then
        mockMvc.perform(get(BASE_URL + "/list"))
            .andDo(print())
            .andExpect(status().isOk());
    }
}
