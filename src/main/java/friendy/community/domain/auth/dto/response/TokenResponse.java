package friendy.community.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
    @Schema(description = "회원 ID", example = "1")
    Long memberId,
    @Schema(description = "액세스 토큰", example = "header.payload.signature")
    String accessToken,
    @Schema(description = "리프레쉬 토큰", example = "header.payload.signature")
    String refreshToken
) {
    public static TokenResponse of(final Long memberId, final String accessToken, final String refreshToken) {
        return new TokenResponse(memberId, accessToken, refreshToken);
    }
}
