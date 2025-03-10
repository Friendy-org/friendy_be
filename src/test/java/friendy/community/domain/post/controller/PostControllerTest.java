package friendy.community.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.post.dto.request.PostCreateRequest;
import friendy.community.domain.post.dto.request.PostUpdateRequest;
import friendy.community.domain.post.dto.response.FindAllPostResponse;
import friendy.community.domain.post.dto.response.FindMemberResponse;
import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.post.service.PostService;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PostService postService;

    private static final String BASE_URL = "/posts";

    private String generateLongContent(int length) {
        return "a".repeat(length);
    }

    @Test
    @DisplayName("게시글 생성 성공 시 201 Created 응답")
    void createPostSuccessfullyReturns201Created() throws Exception {
        // Given
        PostCreateRequest request = new PostCreateRequest("this is new content", List.of("프렌디", "개발", "스터디"), null);
        when(postService.savePost(any(PostCreateRequest.class), any(HttpServletRequest.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/posts/1"));
    }

    @Test
    @DisplayName("게시글 내용이 없으면 400 Bad Request 반환")
    void createPostWithoutContentReturns400BadRequest() throws Exception {
        // Given
        PostCreateRequest request = new PostCreateRequest(null, List.of("프렌디", "개발", "스터디"),null);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 내용이 2200자 초과 시 400 Bad Request 반환")
    void createPostWithContentExceedingMaxLengthReturns400BadRequest() throws Exception {
        // Given
        PostCreateRequest request = new PostCreateRequest(generateLongContent(2300), List.of("프렌디", "개발", "스터디"), null);

        // When & Then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 수정 성공 시 201 Created 응답")
    void updatePostSuccessfullyReturns201Created() throws Exception {
        // Given
        Long postId = 1L;
        PostUpdateRequest request = new PostUpdateRequest("this is updated content", List.of("프렌디", "개발", "스터디"), null);
        when(postService.updatePost(any(PostUpdateRequest.class), any(HttpServletRequest.class), anyLong())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post(BASE_URL + "/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("게시글 수정 시 내용이 2200자 초과하면 400 Bad Request 반환")
    void updatePostWithContentExceedingMaxLengthReturns400BadRequest() throws Exception {
        // Given
        Long postId = 1L;
        PostUpdateRequest request = new PostUpdateRequest(generateLongContent(2300), List.of("프렌디", "개발", "스터디"), null);

        // When & Then
        mockMvc.perform(post(BASE_URL + "/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 삭제 성공 시 200 OK 응답")
    void deletePostSuccessfullyReturns200Ok() throws Exception {
        // Given
        Long postId = 1L;
        doNothing().when(postService).deletePost(any(HttpServletRequest.class), eq(postId));

        // When & Then
        mockMvc.perform(delete(BASE_URL + "/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 조회 성공 시 200 OK 및 게시글 반환")
    void getPostSuccessfullyReturns200Ok() throws Exception {
        // Given
        Long postId = 1L;
        FindPostResponse response = new FindPostResponse(1L, "Post 1", "2025-01-23T10:00:00", 10, 5, 2, new FindMemberResponse(1L, "author1"),null);
        when(postService.getPost(anyLong())).thenReturn(response);

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404 Not Found 반환")
    void getPostWithNonExistentIdReturns404NotFound() throws Exception {
        // Given
        Long nonExistentPostId = 999L;
        when(postService.getPost(anyLong())).thenThrow(new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 게시글입니다."));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/{postId}", nonExistentPostId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("존재하지 않는 게시글입니다."));
    }

    @Test
    @DisplayName("게시글 목록 조회 성공 시 200 OK 반환")
    void getPostsListSuccessfullyReturns200Ok() throws Exception {
        // Given
        List<FindPostResponse> posts = List.of(
                new FindPostResponse(1L, "Post 1", "2025-01-23T10:00:00", 10, 5, 2, new FindMemberResponse(1L, "author1"),null),
                new FindPostResponse(2L, "Post 2", "2025-01-23T11:00:00", 20, 10, 3, new FindMemberResponse(2L, "author2"),null)
        );
        when(postService.getAllPosts(any(Pageable.class)))
                .thenReturn(new FindAllPostResponse(posts, 1));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/list").param("page", "0"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("없는 페이지 요청 시 404 Not Found 반환")
    void getPostsListWithNonExistentPageReturns404NotFound() throws Exception {
        // Given
        when(postService.getAllPosts(any(Pageable.class)))
                .thenThrow(new FriendyException(ErrorCode.RESOURCE_NOT_FOUND, "요청한 페이지가 존재하지 않습니다."));

        // When & Then
        mockMvc.perform(get(BASE_URL + "/list").param("page", "100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("요청한 페이지가 존재하지 않습니다."));
    }
}
