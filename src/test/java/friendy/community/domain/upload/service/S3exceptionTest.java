package friendy.community.domain.upload.service;

import friendy.community.domain.upload.controller.code.UploadExceptionCode;
import friendy.community.global.exception.domain.BadRequestException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.EMPTY_FILE);
    }

    @Test
    @DisplayName("지원되지않는 파일확장자나 파일 이름이 없을경우 예외를 발생시킨다.")
    void throwsExceptionWhenUnsupportedFileExtensionOrMissingFileName() {
        // given
        when(multipartFile.getOriginalFilename()).thenReturn("");
        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.MISSING_FILE_NAME);

        // given
        when(multipartFile.getOriginalFilename()).thenReturn(null);
        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.MISSING_FILE_NAME);

        // given
        when(multipartFile.getOriginalFilename()).thenReturn("test");
        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.UNSUPPORTED_FILE_EXTENSION);

        // given
        when(multipartFile.getOriginalFilename()).thenReturn("test.xyz");
        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.UNSUPPORTED_FILE_EXTENSION);
    }

    @Test
    @DisplayName("파일 크기가 제한을 초과하면 예외를 발생시킨다.")
    void throwsExceptionWhenFileSizeExceedsLimit() {
        // Given
        int MAX_FILE_SIZE = 1024 * 1024;

        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", new byte[MAX_FILE_SIZE + 1]
        );

        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.FILE_SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("지원되지 않는 MIME 타입일 경우 예외를 발생시킨다.")
    void throwsExceptionWhenUnsupportedMimeType() {
        // Given
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.jpg", "application/zip", "test data".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.INVALID_FILE_FORMAT);
    }

    @Test
    @DisplayName("파일의 콘텐츠 타입이 null일 경우 예외를 발생시킨다.")
    void throwsExceptionWhenContentTypeIsNull() {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.jpg", null, "test data".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> s3exception.validateFile(multipartFile))
            .isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.INVALID_FILE_FORMAT);
    }
}