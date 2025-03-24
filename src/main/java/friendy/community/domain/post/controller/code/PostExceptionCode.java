package friendy.community.domain.post.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostExceptionCode implements ExceptionCode {

    POST_NOT_FOUND(4401, "게시글이 없습니다."),
    POST_FORBIDDEN_ACCESS(4402, "게시글은 작성자 본인만 관리할 수 있습니다.");

    private final int code;
    private final String message;
}
