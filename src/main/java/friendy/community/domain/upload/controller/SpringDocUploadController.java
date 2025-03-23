package friendy.community.domain.upload.controller;

import friendy.community.domain.post.dto.response.FindPostResponse;
import friendy.community.domain.upload.dto.response.UploadResponse;
import friendy.community.global.response.FriendyResponse;
import friendy.community.global.swagger.error.ApiErrorResponse;
import friendy.community.global.swagger.error.ErrorCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "업로드 API", description = "업로드 API")
public interface SpringDocUploadController {
    @Operation(summary = "이미지 업로드", description = "이미지를 업로드합니다.")
    @ApiResponse(responseCode = "201", description = "이미지 업로드 성공")
    @ApiErrorResponse(status = HttpStatus.BAD_REQUEST, instance = "/file/upload", errorCases = {
            @ErrorCase(description = "파일 이름이 없거나 확장자가 없습니다.", exampleMessage = "파일 이름이 없거나 확장자가 없습니다."),
            @ErrorCase(description = "지원되지 않는 파일 확장자입니다.", exampleMessage = "지원되지 않는 파일 확장자입니다."),
            @ErrorCase(description = "지원되지 않는 파일 형식입니다.", exampleMessage = "지원되지 않는 파일 형식입니다."),
            @ErrorCase(description = "파일이 비어 있습니다.", exampleMessage = "파일이 비어 있습니다."),
            @ErrorCase(description = "파일 크기가 허용된 범위를 초과했습니다.", exampleMessage = "파일 크기가 허용된 범위를 초과했습니다."),
            @ErrorCase(description = "파일 이름이 없습니다.", exampleMessage = "파일 이름이 없습니다.")
    })
    @ApiErrorResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, instance = "/file/upload", errorCases = {
            @ErrorCase(description = "S3 업로드 중 오류 발생" , exampleMessage = "S3 업로드 중 오류 발생")
    })
    ResponseEntity<FriendyResponse<UploadResponse>> uploadMultipleFile(
            @RequestPart("file") MultipartFile multipartFile);
}