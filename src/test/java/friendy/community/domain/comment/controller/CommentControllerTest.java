package friendy.community.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import friendy.community.domain.comment.CommentType;
import friendy.community.domain.comment.dto.CommentCreateRequest;
import friendy.community.domain.comment.service.CommentService;
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
    @DisplayName("댓글 생성 성공 시 201 Created를 응답한다.")
    void createCommentSuccessfullyReturns201Created() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid comment", CommentType.COMMENT);
        when(commentService.saveComment(any(CommentCreateRequest.class), any(HttpServletRequest.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/comments/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/comments/1"));
    }

    @Test
    @DisplayName("댓글 내용이 없으면 400 Bad Request 반환")
    void createCommentWithoutContentReturns400BadRequest() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest(null, CommentType.COMMENT);

        // When & Then
        mockMvc.perform(post("/comments/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 내용이 1100자 초과 시 400 Bad Request 반환")
    void createCommentWithContentExceedingMaxLengthReturns400BadRequest() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("a".repeat(1200), CommentType.COMMENT);

        // When & Then
        mockMvc.perform(post("/comments/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 종류가 입력되지 않으면 400 Bad Request 반환")
    void createCommentWithoutCommentTypeReturns400BadRequest() throws Exception {
        // Given
        CommentCreateRequest request = new CommentCreateRequest("new valid comment", null);

        // When & Then
        mockMvc.perform(post("/comments/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
