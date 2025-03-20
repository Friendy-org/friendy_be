package friendy.community.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예외 응답")
public record ExceptionResponse(
    @Schema(description = "예외 코드", example = "-9999") int code,
    @Schema(description = "예외 메시지", example = "예외 메시지입니다.") String message) {

    public static ExceptionResponse of(int code, String message) {
        return new ExceptionResponse(code, message);
    }
}
