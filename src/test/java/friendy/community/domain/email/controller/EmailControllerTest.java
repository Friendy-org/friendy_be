package friendy.community.domain.email.controller;

import friendy.community.domain.auth.jwt.JwtTokenFilter;
import friendy.community.domain.email.dto.request.EmailRequest;
import friendy.community.domain.email.dto.request.VerifyCodeRequest;
import friendy.community.domain.email.service.EmailService;
import friendy.community.global.config.MockSecurityConfig;
import friendy.community.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)
    })
@Import(MockSecurityConfig.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("인증 코드 전송 요청이 성공하면 200 OK를 반환한다")
    void sendAuthenticatedEmailSuccessfullyReturnsOk() throws Exception {
        // Given
        EmailRequest request = new EmailRequest("test@example.com");
        doNothing().when(emailService).sendAuthenticatedEmail(request);

        // When & Then
        mockMvc.perform(post("/email/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증 코드 전송 시 이메일이 없으면 400 Bad Request를 반환한다")
    void sendAuthenticatedEmailWithoutEmailReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/email/send-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증 코드 검증 요청이 성공하면 200 OK를 반환한다")
    void verifyAuthCodeSuccessfullyReturnsOk() throws Exception {
        // Given
        VerifyCodeRequest request = new VerifyCodeRequest("test@example.com", "123456");
        doNothing().when(emailService).verifyAuthCode(request);

        // When & Then
        mockMvc.perform(post("/email/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\", \"authCode\":\"123456\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("인증 코드 검증 시 인증번호가 없으면 400 Bad Request를 반환한다")
    void verifyAuthCodeWithoutAuthCodeReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/email/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\"}"))
            .andExpect(status().isBadRequest());
    }
}
