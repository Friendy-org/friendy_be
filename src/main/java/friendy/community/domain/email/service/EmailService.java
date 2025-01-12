package friendy.community.domain.email.service;

import friendy.community.domain.email.dto.request.EmailRequest;
import friendy.community.domain.email.dto.request.VerifyCodeRequest;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private static final long CODE_EXPIRE_MILLIS = 300000;  // 5분

    public void sendAuthenticatedEmail(final EmailRequest request) {
        try {
            final String code = generateAndSaveAuthCode(request.email());
            final MimeMessage message = createEmailMessage(request.email(), code);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new FriendyException(ErrorCode.INTERNAL_SERVER_ERROR, "이메일 전송에 실패했습니다.");
        }
    }

    private String generateAndSaveAuthCode(final String email) {
        final String code = generateAuthCode();
        saveAuthCode(email, code);
        return code;
    }

    private MimeMessage createEmailMessage(final String toEmail, final String code) throws MessagingException {
        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject("Friendy Community 이메일 인증 코드");
        helper.setText(getEmailContent(code), true);
        return message;
    }

    private String generateAuthCode() {
        final int maxRange = 1000000;
        final Random random = new Random();
        return String.format("%06d", random.nextInt(maxRange));
    }

    private void saveAuthCode(final String email, final String code) {
        redisTemplate.opsForValue().set(email, code, CODE_EXPIRE_MILLIS, TimeUnit.MILLISECONDS);
    }

    private String getEmailContent(final String authCode) {
        final Context context = new Context();
        context.setVariable("authCode", authCode);
        return templateEngine.process("auth-email", context);
    }

}
