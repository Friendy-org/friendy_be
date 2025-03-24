package friendy.community.domain.upload.controller;

import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.global.config.MockSecurityConfig;
import friendy.community.global.config.SecurityConfig;
import friendy.community.domain.upload.service.S3service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UploadController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)
    })
@Import(MockSecurityConfig.class)
public class UploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3service s3service;

    @InjectMocks
    private UploadController uploadController;

    @Test
    @DisplayName("파일 업로드 시 파일 URL을 반환한다.")
    void uploadFileReturnFileUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});
        String mockFileUrl = "https://example.com/test.jpg";

        // When
        when(s3service.upload(file, "temp")).thenReturn(mockFileUrl);
        
        // Then
        mockMvc.perform(multipart("/file/upload")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().string(mockFileUrl));
    }
}