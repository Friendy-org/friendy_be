package friendy.community.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "사용자 회원가입")
public record MemberSignUpRequest(

        @Schema(description = "이메일", example = "example@friendy.com")
        @NotBlank(message = "이메일이 입력되지 않았습니다.")
        @Email(message = "이메일 형식으로 입력해주세요.")
        String email,

        @Schema(description = "닉네임", example = "bokSungKim")
        @NotBlank(message = "닉네임이 입력되지 않았습니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이로 입력해주세요.")
        String nickname,

        @Schema(description = "비밀번호. 숫자, 영문자, 특수문자(~!@#$%^&*?)를 반드시 포함해야 합니다.", example = "password123!")
        @NotBlank(message = "비밀번호가 입력되지 않았습니다.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[~!@#$%^&*?]).*$", message = "숫자, 영문자, 특수문자(~!@#$%^&*?)를 포함해야 합니다.")
        @Size(min = 8, max = 16, message = "비밀번호는 8~16자 사이로 입력해주세요.")
        String password,

        @Schema(description = "생년월일", example = "2002-08-13")
        @NotNull(message = "생년월일이 입력되지 않았습니다.")
        LocalDate birthDate,

        @Schema(description = "프로필 이미지 URL (선택 사항)", example = "https://example.com/profile.jpg")
        @Pattern(regexp = "^(https?|ftp)://.*$", message = "유효한 URL 형식이어야 합니다.")
        @Size(max = 255, message = "이미지 URL은 255자 이내로 입력해주세요.")
        String imageUrl

) {
}