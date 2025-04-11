package friendy.community.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "게시글 수정")
public record PostUpdateRequest(

    @Schema(description = "게시글 내용", example = "프렌디게시글내용")
    @NotBlank(message = "게시글 내용이 입력되지 않았습니다.")
    @Size(max = 2200, message = "게시글은 2200자 이내로 작성해주세요.")
    String content,

    @Schema(description = "게시글 해시태그", example = "[\"프렌디\", \"개발\", \"스터디\"]")
    List<String> hashtags,

    @Schema(
        description = "게시글 이미지 목록",
        example = "[\"https://friendybucket.s3.us-east-2.amazonaws.com/test/3a4abd13-a8b5-47b5-8a9d-28fb21b8929b.jpg\", \"https://friendybucket.s3.us-east-2.amazonaws.com/test/f851b0ff-800d-4d1a-8c75-a65a6d462bde.jpg\"]"
    )
    List<String> imageUrls,
    
    @Schema(description = "위치", example = "부산 광역시")
    String location
) {
}
