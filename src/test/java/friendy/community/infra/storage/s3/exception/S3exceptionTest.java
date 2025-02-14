package friendy.community.infra.storage.s3.exception;

import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@DirtiesContext
class S3exceptionTest {

    @Autowired
    private S3exception s3exception;

    @Mock
    private MultipartFile multipartFile;

    @Test
    @DisplayName("파일이 비어있을 때 예외가 발생한다.")
    void thorwsExceptionWhenFileDosentExists() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // Then
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("파일이 비어 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("파일 크기가 제한을 초과하면 예외를 발생시킨다.")
    void throwsExceptionWhenUnsupportedFileExtension() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("test.xyz");

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // Then
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("지원되지 않는 파일 확장자입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("파일 크기가 제한을 초과하면 예외를 발생시킨다.")
    void throwsExceptionWhenFileSizeExceedsLimit() {
        // Given
        int MAX_FILE_SIZE = 1024 * 1024;

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[MAX_FILE_SIZE+1]
        );

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // Then
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("파일 크기가 허용된 범위를 초과했습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("지원되지 않는 MIME 타입일 경우 예외를 발생시킨다.")
    void throwsExceptionWhenUnsupportedMimeType() {
        // Given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "application/zip", "test data".getBytes()
        );

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // Then
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("지원되지 않는 파일 형식입니다.", exception.getMessage());
    }
}