package friendy.community.domain.member.encryption;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SHA2PasswordEncryptorTest {

    private final SHA2PasswordEncryptor passwordEncryptor = new SHA2PasswordEncryptor();

    @Test
    @DisplayName("평문 비밀번호와 salt를 받아 암호화된 문자열을 반환하면 성공")
    void encryptReturnsEncryptedPassword() {
        // Given
        String plainPassword = "password123";
        String salt = "randomSalt";

        // When
        String encryptedPassword = passwordEncryptor.encrypt(plainPassword, salt);

        // Then
        assertThat(encryptedPassword).isNotBlank();
        assertThat(Base64.getDecoder().decode(encryptedPassword)).isNotEmpty();
    }

    @Test
    @DisplayName("동일한 평문, 동일한 salt 값에 대해 항상 동일한 암호화 값을 반환하면 성공")
    void encryptReturnsConsistentResultForSameInput() {
        // Given
        String plainPassword = "password123";
        String salt = "randomSalt";

        // When
        String encryptedPassword1 = passwordEncryptor.encrypt(plainPassword, salt);
        String encryptedPassword2 = passwordEncryptor.encrypt(plainPassword, salt);

        // Then
        assertThat(encryptedPassword1).isEqualTo(encryptedPassword2);
    }

    @Test
    @DisplayName("다른 평문, 동일한 salt 값에 대해 다른 암호화 값을 반환하면 성공")
    void encryptReturnsDifferentResultsForDifferentInputs() {
        // Given
        String plainPassword1 = "password123";
        String plainPassword2 = "differentPassword";
        String salt = "randomSalt";

        // When
        String encryptedPassword1 = passwordEncryptor.encrypt(plainPassword1, salt);
        String encryptedPassword2 = passwordEncryptor.encrypt(plainPassword2, salt);

        // Then
        assertThat(encryptedPassword1).isNotEqualTo(encryptedPassword2);
    }

    @Test
    @DisplayName("동일한 평문, 다른 salt 값에 대해 다른 암호화 값을 반환하면 성공")
    void encryptReturnsDifferentResultsForSamePasswordWithDifferentSalts() {
        // Given
        String plainPassword = "password123";
        String salt1 = "randomSalt1";
        String salt2 = "randomSalt2";

        // When
        String encryptedPassword1 = passwordEncryptor.encrypt(plainPassword, salt1);
        String encryptedPassword2 = passwordEncryptor.encrypt(plainPassword, salt2);

        // Then
        assertThat(encryptedPassword1).isNotEqualTo(encryptedPassword2);
    }

}