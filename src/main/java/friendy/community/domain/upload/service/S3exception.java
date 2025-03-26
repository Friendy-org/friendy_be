package friendy.community.domain.upload.service;

import friendy.community.domain.upload.controller.code.UploadExceptionCode;
import friendy.community.global.exception.domain.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Component
public class S3exception {

    private static final long MAX_FILE_SIZE = 1024 * 1024;

    public void validateFile(MultipartFile multipartFile) {
        validateEmptyFile(multipartFile);
        validateFileExtension(multipartFile);
        validateMimeType(multipartFile);
        validateFileSize(multipartFile);
    }

    private void validateMimeType(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType == null || !isAllowedMimeType(contentType)) {
            throw new BadRequestException(UploadExceptionCode.INVALID_FILE_FORMAT);
        }
    }

    private void validateEmptyFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new BadRequestException(UploadExceptionCode.EMPTY_FILE);
        }
    }

    private void validateFileExtension(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();

        if (fileName == null || fileName.isEmpty()) {
            throw new BadRequestException(UploadExceptionCode.MISSING_FILE_NAME);
        }

        if (!fileName.contains(".")) {
            throw new BadRequestException(UploadExceptionCode.UNSUPPORTED_FILE_EXTENSION);
        }

        String[] allowedExtensions = {"jpg", "png", "gif", "pdf"};
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(); // 확장자 추출

        if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
            throw new BadRequestException(UploadExceptionCode.UNSUPPORTED_FILE_EXTENSION);
        }
    }

    private void validateFileSize(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException(UploadExceptionCode.FILE_SIZE_EXCEEDED);
        }
    }

    private boolean isAllowedMimeType(String contentType) {
        return List.of("image/jpeg", "image/png", "image/gif").contains(contentType);
    }
}