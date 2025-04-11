package friendy.community.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MemberUpdateRequest(
    @Schema(description = "닉네임 변경", example = "newBokSungKim")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이로 입력해주세요.")
    String nickname,

    @Schema(description = "생년월일 변경", example = "2012-08-13")
    LocalDate birthDate,

    @Schema(description = "프로필 이미지 URL (선택 사항)", example = "https://friendybucket.s3.us-east-2.amazonaws.com/test/7f96f3a9-37aa-48e5-b49f-76ab0201d78c.jpg")
    @Pattern(regexp = "^(https?|ftp)://.*$", message = "유효한 URL 형식이어야 합니다.")
    @Size(max = 255, message = "이미지 URL은 255자 이내로 입력해주세요.")
    String imageUrl
) {
}
