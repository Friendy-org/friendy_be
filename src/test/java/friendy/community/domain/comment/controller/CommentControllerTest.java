package friendy.community.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.CommentUpdateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
import friendy.community.domain.comment.service.CommentService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)
    })
@Import(MockSecurityConfig.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CommentService commentService;

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
    @DisplayName("댓글 생성 성공 시 200 Ok를 응답")
    void createCommentSuccessfullyReturns201Created() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid comment", 1L);

        // When & Then
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 내용이 없으면 400 Bad Request 응답")
    void createCommentWithoutContentReturns400BadRequest() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest(null, 1L);

        // When & Then
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 내용이 1100자 초과 시 400 Bad Request 응답")
    void createCommentWithContentExceedingMaxLengthReturns400BadRequest() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("a".repeat(1200), 1L);

        // When & Then
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글이 달릴 게시글이 명시되지 않으면 400 Bad Request 응답")
    void createCommentWithoutPostIdReturns400BadRequest() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid content", null);

        // When & Then
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("답글 생성 성공 시 200 Ok 응답")
    void createReplySuccessfullyReturns200Ok() throws Exception {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("new valid reply", 1L, 1L);

        // When & Then
        mockMvc.perform(post("/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("답글 내용이 없으면 400 Bad Request 응답")
    void createReplyWithoutContentReturns400BadRequest() throws Exception {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("", 1L, 1L);

        // When & Then
        mockMvc.perform(post("/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("답글 내용이 1100자 초과 시 400 Bad Request 응답")
    void createReplyWithContentExceedingMaxLengthReturns400BadRequest() throws Exception {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("a".repeat(1200), 1L, 1L);

        // When & Then
        mockMvc.perform(post("/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("답글이 달릴 게시글이 명시되지 않으면 400 Bad Request 응답")
    void createReplyWithoutPostIdReturns400BadRequest() throws Exception {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("new valid content", null, 1L);

        // When & Then
        mockMvc.perform(post("/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("답글이 달릴 댓글이 명시되지 않으면 400 Bad Request 응답")
    void createReplyWithoutCommentIdReturns400BadRequest() throws Exception {
        // Given
        ReplyCreateRequest request = new ReplyCreateRequest("new valid content", 1L, null);

        // When & Then
        mockMvc.perform(post("/comments/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 수정 성공 시 200 Ok 응답")
    void updateCommentSuccessfullyReturns200Ok() throws Exception {
        // Given
        Long commentId = 1L;
        CommentUpdateRequest updateRequest = new CommentUpdateRequest("edited valid content");

        doNothing().when(commentService).updateComment(any(CommentUpdateRequest.class), eq(commentId), anyLong());

        // When & Then
        mockMvc.perform(post("/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("빈 내용으로 댓글 수정 요청 시 400 Bad Request 응답")
    void updateCommentWithoutContentReturns400BadRequest() throws Exception {
        // Given
        Long commentId = 1L;
        CommentUpdateRequest updateRequest = new CommentUpdateRequest("");

        doNothing().when(commentService).updateComment(any(CommentUpdateRequest.class), eq(commentId), anyLong());

        // When & Then
        mockMvc.perform(post("/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 수정 요청의 내용이 제한을 초과하는 경우 400 Bad Request 응답")
    void updateCommentWithExceedingContentsLengthLimitReturns400BadRequest() throws Exception {
        // Given
        Long commentId = 1L;
        CommentUpdateRequest updateRequest = new CommentUpdateRequest("a".repeat(1200));

        doNothing().when(commentService).updateComment(any(CommentUpdateRequest.class), eq(commentId), anyLong());

        // When & Then
        mockMvc.perform(post("/comments/{commentId}", commentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("답글 수정 성공 시 200 Ok 응답")
    void updateReplySuccessfullyReturns200Ok() throws Exception {
        // Given
        Long replyId = 1L;
        CommentUpdateRequest updateRequest = new CommentUpdateRequest("new valid content");

        doNothing().when(commentService).updateReply(any(CommentUpdateRequest.class), eq(replyId), anyLong());

        // When & Then
        mockMvc.perform(post("/comments/reply/{replyId}", replyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 삭제 성공 시 200 Ok 응답")
    void deleteCommentSuccessfullyReturns200Ok() throws Exception {
        // Given
        Long commentId = 1L;

        doNothing().when(commentService).deleteComment(eq(commentId), anyLong());

        // When & Then
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("답글 삭제 성공 시 200 Ok 응답")
    void deleteReplySuccessfullyReturns200Ok() throws Exception {
        // Given
        Long replyId = 1L;

        doNothing().when(commentService).deleteReply(eq(replyId), anyLong());

        // When & Then
        mockMvc.perform(delete("/comments/reply/{replyId}", replyId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
