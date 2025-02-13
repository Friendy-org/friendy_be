package friendy.community.domain.upload.controller;

import friendy.community.infra.storage.s3.service.S3service;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class) // Controller만 테스트
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3service s3service; // S3service Mock

    @InjectMocks
    private UploadController uploadController;

    @Test
    void uploadMultipleFile_ShouldReturnFileUrl() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});
        String mockFileUrl = "https://example.com/test.jpg";

        // Mocking S3service.upload() 메서드
        when(s3service.upload(file, "temp")).thenReturn(mockFileUrl);

        // when & then
        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk()) // HTTP 상태가 200 OK여야 함
                .andExpect(content().string(mockFileUrl)); // 응답 본문에 파일 URL이 포함되어야 함
    }
}