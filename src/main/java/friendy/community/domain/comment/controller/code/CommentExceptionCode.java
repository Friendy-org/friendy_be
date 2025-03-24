package friendy.community.domain.comment.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentExceptionCode implements ExceptionCode {

    UNAUTHORIZED_COMMENT_USER(4501, "작성자만 댓글을 수정할 수 있습니다."),
    UNAUTHORIZED_REPLY_USER(4502, "작성자만 답글을 수정할 수 있습니다."),
    COMMENT_NOT_FOUND(4503, "존재하지 않는 댓글입니다."),
    REPLY_NOT_FOUND(4504, "존재하지 않는 답글입니다."),
    POST_NOT_FOUND(4504, "게시글을 찾을 수 없습니다.");

    private final int code;
    private final String message;

}
