package friendy.community.domain.upload.controller.code;

import friendy.community.global.exception.dto.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadExceptionCode implements ExceptionCode {

    INVALID_FILE_FORMAT(4701, "지원되지 않는 파일 형식입니다."),
    EMPTY_FILE(4702, "파일이 비어 있습니다."),
    MISSING_FILE_NAME(4703, "파일 이름이 없습니다."),
    UNSUPPORTED_FILE_EXTENSION(4704, "지원되지 않는 파일 확장자입니다."),
    FILE_SIZE_EXCEEDED(4705, "파일 크기가 허용된 범위를 초과했습니다."),
    INVALID_URL_FORMAT(4706, "유효한 URL 형식이어야 합니다."),
    FILE_TYPE_UNAVAILABLE(4707, "파일타입을 가져올 수 없습니다."),
    FILE_DELETION_FAILED(4708, "파일을 삭제하지 못했습니다."),
    URL_FORMAT_INVALID(4709, "유효한 URL 형식이어야 합니다."),
    S3_UPLOAD_ERROR(4710, "S3 업로드 중 오류 발생"),
    S3_OBJECT_COPY_FAILED(4711, "S3 객체 복사에 실패했습니다.");

    private final int code;
    private final String message;
}
