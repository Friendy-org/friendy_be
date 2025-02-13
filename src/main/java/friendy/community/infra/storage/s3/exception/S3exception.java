package friendy.community.infra.storage.s3.exception;

import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
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
        if (contentType != null && !isAllowedMimeType(contentType)) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "지원되지 않는 파일 형식입니다.");
        }
    }

    private void validateEmptyFile(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "파일이 비어 있습니다.");
        }
    }

    private void validateFileExtension(MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();

        if (fileName == null || fileName.isEmpty()) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "파일 이름이 없습니다.");
        }

        if (!fileName.contains(".")) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "확장자가 없습니다.");
        }


        String[] allowedExtensions = {"jpg", "png", "gif", "pdf"};
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(); // 확장자 추출

        if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "지원되지 않는 파일 확장자입니다.");
        }
    }

    private void validateFileSize(MultipartFile multipartFile) {
        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "파일 크기가 허용된 범위를 초과했습니다.");
        }
    }

    private boolean isAllowedMimeType(String contentType) {
        return List.of("image/jpeg", "image/png", "image/gif").contains(contentType);
    }

}