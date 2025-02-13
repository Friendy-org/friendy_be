package friendy.community.domain.member.fixture;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.encryption.SHA2PasswordEncryptor;
import friendy.community.domain.member.model.Member;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;

public class MemberFixture {

    private static final PasswordEncryptor passwordEncryptor = new SHA2PasswordEncryptor();

    public static Member memberFixture() {
        String encrypted = passwordEncryptor.encrypt(getFixturePlainPassword(), "salt");
        MemberSignUpRequest request = new MemberSignUpRequest(
                "example@friendy.com",
                "bokSungKim",
                "password123!",
                LocalDate.parse("2002-08-13"),
                "https://test.com/test");
        return new Member(request,encrypted,"salt");
    }

    public static MockMultipartFile createJsonRequest(String email, String nickname, String password, LocalDate birthDate) {
        String jsonRequest = String.format("""
        {
          "email": "%s",
          "nickname": "%s",
          "password": "%s",
          "birthDate": "%s"
        }
        """, email, nickname, password, birthDate);


        return new MockMultipartFile(
                "request",
                "",
                "application/json",
                jsonRequest.getBytes()
        );
    }

    public static MockMultipartFile createImageFile(String fileName, String contentType, byte[] content) {
        return new MockMultipartFile(
                "image",
                fileName,
                contentType,
                content
        );
    }

    public static String getFixturePlainPassword() {
        return "password123!";
    }

}