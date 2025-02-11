package friendy.community.domain.comment.dto;

import friendy.community.domain.comment.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 작성")
public record CommentUpdateRequest(
        @Schema(description = "댓글 내용", example = "프렌디댓글내용")
        @NotBlank(message = "댓글 내용이 입력되지 않았습니다.")
        @Size(max = 1100, message = "댓글은 1100자 이내로 작성해주세요.")
        String content,

        @Schema(description = "댓글 종류", example = "COMMENT or REPLY")
        @NotBlank(message = "댓글 종류가 입력되지 않았습니다.")
        CommentType type
) {
}
