package friendy.community.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiResponse", description = "공통 API 응답 포맷")
public record FriendyResponse<T>(
    @Schema(description = "응답 코드", example = "200")
    int code,

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    String message,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "결과 데이터", example = "결과값이 여기에 포함될 수 있습니다.")
    @Nullable T result  // Optional 제거
) {
    public static <T> FriendyResponse<T> of(int code, String message) {
        return new FriendyResponse<>(code, message, null);  // 결과값이 없을 경우 null 처리
    }

    public static <T> FriendyResponse<T> of(int code, String message, T result) {
        return new FriendyResponse<>(code, message, result);
    }
}
