package friendy.community.domain.comment.dto;

import friendy.community.domain.comment.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 수정")
public record CommentUpdateRequest(
        @Schema(description = "댓글 내용", example = "수정된 댓글 내용")
        @NotBlank(message = "댓글 내용이 입력되지 않았습니다.")
        @Size(max = 1100, message = "댓글은 1100자 이내로 작성해주세요.")
        String content
) {
}
