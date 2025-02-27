package friendy.community.domain.member.fixture;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.encryption.PasswordEncryptor;
import friendy.community.domain.member.encryption.SHA2PasswordEncryptor;
import friendy.community.domain.member.model.Member;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public static String getFixturePlainPassword() {
        return "password123!";
    }

    public static List<Member> createMultipleMembers(int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String encryptedPassword = passwordEncryptor.encrypt("password" + i + "!", "salt");
            MemberSignUpRequest request = new MemberSignUpRequest(
                "user" + i + "@friendy.com",
                "nickname" + i,
                "password" + i + "!",
                LocalDate.of(2000 + (i % 20), (i % 12) + 1, (i % 28) + 1),
                "https://test.com/user" + i + ".jpg"
            );
            members.add(new Member(request, encryptedPassword, "salt"));
        }
        return members;
    }
}