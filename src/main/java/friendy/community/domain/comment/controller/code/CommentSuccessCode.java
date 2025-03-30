package friendy.community.domain.comment.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentSuccessCode implements ApiCode {

    COMMENT_CREATE_SUCCESS(1501, "댓글 작성 성공"),
    REPLY_CREATE_SUCCESS(1502, "답글 작성 성공"),
    COMMENT_UPDATE_SUCCESS(1503, "댓글 수정 성공"),
    REPLY_UPDATE_SUCCESS(1504, "답글 수정 성공"),

    REPLY_GET_ALL_SUCCESS(1506, "답글 조회 성공");

    private final int code;
    private final String message;
}
