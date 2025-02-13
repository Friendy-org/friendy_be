package friendy.community.infra.storage.s3.exception;

import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class S3exceptionTest {

    @Autowired
    private S3exception s3exception;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        s3exception = new S3exception();
    }

    @Test
    void validateFile_파일이비어있을때_예외() {
        // Given
        when(multipartFile.isEmpty()).thenReturn(true);

        // When & Then
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // 예외 검증
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("파일이 비어 있습니다.", exception.getMessage());
    }

    @Test
    void validateFile_지원되지않는확장자일때_예외() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("test.xyz");

        // When & Then
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // 예외 검증
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("지원되지 않는 파일 확장자입니다.", exception.getMessage());
    }

    @Test
    void validateFile_파일크기가초과할때_예외() {
        // Given
        int MAX_FILE_SIZE = 1024 * 1024;

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[MAX_FILE_SIZE+1] // 파일 이름과 MIME 타입 설정
        );

        // When & Then
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3exception.validateFile(multipartFile);
        });

        // 예외 검증
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("파일 크기가 허용된 범위를 초과했습니다.", exception.getMessage());
    }

    @Test
    void validateFile_지원되지않는MIME타입일때_예외() {
        // Given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "application/zip", "test data".getBytes() // 파일 이름과 MIME 타입 설정
        );

        // When & Then
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            // multipartFile을 mock으로 사용할 때 getContentType() 메서드를 호출하도록 설정
            s3exception.validateFile(multipartFile);  // 예외가 발생해야 한다.
        });

        // 예외 검증
        assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        assertEquals("지원되지 않는 파일 형식입니다.", exception.getMessage());
    }
}