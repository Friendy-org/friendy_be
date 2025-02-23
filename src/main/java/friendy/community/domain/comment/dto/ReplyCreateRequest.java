package friendy.community.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "답글 작성")
public record ReplyCreateRequest(
        @Schema(description = "답글 내용", example = "프렌디 답글 내용")
        @NotBlank(message = "답글 내용이 입력되지 않았습니다.")
        @Size(max = 1100, message = "답글은 1100자 이내로 작성해주세요.")
        String content,

        @Schema(description = "답글 대상 게시글 id", example = "1")
        @NotNull(message = "답글이 달릴 게시글이 명시되지 않았습니다.")
        Long postId,

        @Schema(description = "답글 대상 댓글 id", example = "1")
        @NotNull(message = "답글이 달릴 댓글이 명시되지 않았습니다.")
        Long commentId
) {
}
