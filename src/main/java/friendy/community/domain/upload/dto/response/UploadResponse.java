package friendy.community.domain.upload.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record UploadResponse(
        @Schema(description = "업로드된 이미지의 URL", example = "https://example.com/uploaded-image.jpg")
        String imageUrl
) {
}