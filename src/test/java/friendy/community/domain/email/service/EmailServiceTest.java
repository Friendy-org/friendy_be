package friendy.community.domain.email.service;

import friendy.community.domain.email.dto.request.EmailRequest;
import friendy.community.domain.email.dto.request.VerifyCodeRequest;
import friendy.community.global.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@SpringBootTest
@Transactional
@DirtiesContext
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private SpringTemplateEngine templateEngine;

    @MockitoBean
    private MimeMessage mimeMessage;

    @Test
    @DisplayName("이메일 전송이 성공하면 Redis에 인증 코드가 저장된다")
    void sendAuthenticatedEmailSuccessfullyStoresCodeInRedis() throws Exception {
        // Given
        EmailRequest request = new EmailRequest("test@example.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Redis Mock 설정
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // TemplateEngine Mock 설정
        when(templateEngine.process(anyString(), any())).thenReturn("<html>dummy content</html>");

        // When
        emailService.sendAuthenticatedEmail(request);

        // Then
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(valueOperations, times(1)).set(
                eq(request.email()),
                anyString(),
                eq(300000L),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("인증번호 검증이 성공하면 예외가 발생하지 않는다")
    void verifyAuthCodeSuccessfully() {
        // Given
        String email = "test@example.com";
        String authCode = "123456";
        VerifyCodeRequest request = new VerifyCodeRequest(email, authCode);

        // Mock 설정 추가
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn(authCode);

        // When & Then (예외가 발생하지 않으면 성공)
        emailService.verifyAuthCode(request);
    }

    @Test
    @DisplayName("Redis에 저장된 인증번호가 없으면 FriendyException을 던진다")
    void throwsExceptionWhenAuthCodeNotFound() {
        // Given
        String email = "test@example.com";
        VerifyCodeRequest request = new VerifyCodeRequest(email, "123456");

        // Mock 설정 추가
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> emailService.verifyAuthCode(request))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증번호가 존재하지 않습니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("입력된 인증번호가 저장된 인증번호와 일치하지 않으면 FriendyException을 던진다")
    void throwsExceptionWhenAuthCodeDoesNotMatch() {
        // Given
        String email = "test@example.com";
        VerifyCodeRequest request = new VerifyCodeRequest(email, "123456");

        // Mock 설정 추가
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(email)).thenReturn("654321");

        // When & Then
        assertThatThrownBy(() -> emailService.verifyAuthCode(request))
                .isInstanceOf(FriendyException.class)
                .hasMessageContaining("인증번호가 일치하지 않습니다.")
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REQUEST);
    }

}
