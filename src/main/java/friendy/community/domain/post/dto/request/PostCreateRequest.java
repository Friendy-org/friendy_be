package friendy.community.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "게시판 생성")
public record PostCreateRequest(

        @Schema(description = "게시글 내용", example = "프렌디게시글내용")
        @NotBlank(message = "게시글 내용이 입력되지 않았습니다.")
        @Size(max = 2200, message = "게시글은 2200자 이내로 작성해주세요.")
        String content,

        @Schema(description = "게시글 해시태그", example = "[\"프렌디\", \"개발\", \"스터디\"]")
        List<String> hashtags,

        @Schema(
            description = "게시글 이미지 목록",
            example = "[\"https://friendybucket.s3.us-east-2.amazonaws.com/test/d08f0a96-3386-413e-96c1-bc85cfdaa2fc.jpg\", \"https://friendybucket.s3.us-east-2.amazonaws.com/test/d08f0a96-3386-413e-96c1-bc85cfdaa2fc.jpg\"]"
        )
        @NotEmpty(message = "게시글이미지는 최소 1개 이상 등록해야 합니다.")
        List<String> imageUrls,

        @Schema(description = "위치", example = "부산 광역시")
        String location
) {
}