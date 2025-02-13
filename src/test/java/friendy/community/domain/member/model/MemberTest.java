package friendy.community.domain.member.model;

import friendy.community.domain.member.dto.request.MemberSignUpRequest;
import friendy.community.domain.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    @DisplayName("Member.of 메서드는 요청 데이터를 바탕으로 Member 객체를 생성한다")
    void ofMethodCreatesMemberFromRequest() {
        // Given
        MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest("example@friendy.com", "bokSungKim", "password123!", LocalDate.parse("2002-08-13"),null);
        String encryptedPassword = "securePassword123!";
        String salt = "randomSalt";

        // When
        Member member = Member.of(memberSignUpRequest, encryptedPassword, salt);

        // Then
        assertThat(member.getEmail()).isEqualTo(memberSignUpRequest.email());
        assertThat(member.getNickname()).isEqualTo(memberSignUpRequest.nickname());
        assertThat(member.getPassword()).isEqualTo(encryptedPassword);
        assertThat(member.getSalt()).isEqualTo(salt);
        assertThat(member.getBirthDate()).isEqualTo(memberSignUpRequest.birthDate());
    }

    @Test
    @DisplayName("resetPassword 메서드는 해당 객체의 비밀번호를 요청한 새 비밀번호로 변경한다")
    void resetPasswordMethodChangesPasswordFromRequestPassword() {
        // Given
        Member member = MemberFixture.memberFixture();
        String newEncryptedPassword = "newSecurePw123!";

        // When
        member.resetPassword(newEncryptedPassword, member.getSalt());

        // Then
        assertThat(member.getPassword()).isEqualTo(newEncryptedPassword);
    }

}