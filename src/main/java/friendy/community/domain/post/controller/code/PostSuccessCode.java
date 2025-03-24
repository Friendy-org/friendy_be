package friendy.community.domain.post.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostSuccessCode implements ApiCode {

    CREATE_POST_SUCCESS(1401, "게시글 생성 성공"),
    UPDATE_POST_SUCCESS(1402, "게시글 수정 성공"),
    DELETE_POST_SUCCESS(1403, "게시글 삭제 성공"),
    GET_POST_SUCCESS(1404, "게시글 조회 성공"),
    GET_ALL_POSTS_SUCCESS(1405, "게시글 목록 조회 성공");

    private final int code;
    private final String message;
}
