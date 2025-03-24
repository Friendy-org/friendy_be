package friendy.community.domain.upload.controller.code;

import friendy.community.global.response.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadSuccessCode implements ApiCode {

    IMAGE_UPLOAD_SUCCESS(1701, "이미지 업로드 성공");

    private final int code;
    private final String message;
}
