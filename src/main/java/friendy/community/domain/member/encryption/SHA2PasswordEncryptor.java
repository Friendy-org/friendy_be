package friendy.community.domain.member.encryption;

import friendy.community.domain.member.controller.code.MemberExceptionCode;
import friendy.community.global.exception.domain.BadRequestException;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SHA2PasswordEncryptor implements PasswordEncryptor {

    private final MessageDigest digest;

    public SHA2PasswordEncryptor() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestException(MemberExceptionCode.INVALID_ENCRYPTION_ALGORITHM);
        }
    }

    @Override
    public String encrypt(String plainPassword, String salt) {
        String passwordWithSalt = plainPassword + salt;
        byte[] encryptByte = digest.digest(passwordWithSalt.getBytes());
        return Base64.getEncoder().encodeToString(encryptByte);
    }
}
