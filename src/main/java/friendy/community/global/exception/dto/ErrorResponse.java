package friendy.community.global.exception.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요청 검증 에러 응답")
public record ErrorResponse(
        @Schema(description = "검증 실패 필드", example = "필드 네임")
        String fieldName,
        @Schema(description = "검증 실패 에러 메시지", example = "필드 타입이 잘못되었습니다.")
        String message
) {

    private static final String MESSAGE_FORMAT = "필드명: %s, 예외 메세지: %s";

    @Override
    public String toString() {
        return String.format(MESSAGE_FORMAT, fieldName, message);
    }

}
