package friendy.community.domain.member.encryption;

import friendy.community.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class SHA2PasswordEncryptor implements PasswordEncryptor{

    private final MessageDigest digest;

    public SHA2PasswordEncryptor() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new FriendyException(ErrorCode.INTERNAL_SERVER_ERROR, "암호화 알고리즘이 잘못 명시되었습니다.");
        }
    }

    @Override
    public String encrypt(String plainPassword, String salt) {
        String passwordWithSalt = plainPassword + salt;
        byte[] encryptByte = digest.digest(passwordWithSalt.getBytes());
        return Base64.getEncoder().encodeToString(encryptByte);
    }
}
