package friendy.community.domain.comment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.comment.CommentType;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.dto.ReplyCreateRequest;
import friendy.community.domain.comment.service.CommentService;
import friendy.community.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

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
    void createReplyWithContentExeedingMaxLengthReturns400BadRequest() throws Exception {
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
}
