package friendy.community.infra.storage.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import friendy.community.global.exception.ErrorCode;
import friendy.community.global.exception.FriendyException;
import friendy.community.infra.storage.s3.exception.S3exception;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@DirtiesContext
class S3serviceTest {

    @Autowired
    private S3service s3service;

    @MockitoBean
    private AmazonS3 s3Client;

    @Mock
    private S3exception s3exception;

    @Mock
    MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("S3 파일 업로드가 성공하면 URL을 반환한다.")
    void uploadShouldReturnUrlWhenSuccessful() throws MalformedURLException {
        // Given
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

        String tempUrl = "https://your-bucket.s3.amazonaws.com/test.jpg";
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL(tempUrl));

        // When
        String actualUrl = s3service.upload(multipartFile, "test-dir");

        // Then
        assertThat(actualUrl).isEqualTo(tempUrl);
    }


    @Test
    @DisplayName("S3 객체 이동이 성공하면 새로운 URL을 반환한다.")
    void moveS3ObjectShouldReturnNewUrlWhenSuccessful() throws MalformedURLException {
        // Given
        String imageUrl = "https://your-bucket.s3.amazonaws.com/old-dir/test.jpg";
        String newDirName = "new-dir";
        String expectedUrl = "https://your-bucket.s3.amazonaws.com/new-dir/test.jpg";

        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL(expectedUrl));

        // When
        String actualUrl = s3service.moveS3Object(imageUrl, newDirName);

        // Then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(s3Client).copyObject(any());
    }

    @Test
    @DisplayName("파일 이름을 찾을 수 없을 때 예외를 발생시킨다.")
    void throwsExceptionWhenFileNameIsNull() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        assertThrows(FriendyException.class, () -> {
            s3service.generateStoredFileName(multipartFile,"upload");
        });
    }

    @Test
    @DisplayName("정상적인 파일 타입을 S3에서 가져오면 파일 타입을 반환한다.")
    void moveS3ObjectShouldReturnFileTypeWhenSuccessful() {
        // Given
        S3Object s3Object = new S3Object();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");
        s3Object.setObjectMetadata(metadata);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);

        // When
        String result = s3service.getContentTypeFromS3("test");

        // Then
        assertThat(result).isEqualTo("image/png");
    }

    @Test
    @DisplayName("정상적인 파일 타입을 S3에서 가져오지 못하면 예외가 발생한다.")
    void throwsExceptionWhenGetContentTypeFromS3Fails() {
        // Given
        when(s3Client.getObject(any(GetObjectRequest.class))).
                thenThrow(new FriendyException(ErrorCode.INVALID_FILE,"파일타입을 가져올수 없습니다."));

        // When & Then
        assertThrows(FriendyException.class, () -> {
            s3service.getContentTypeFromS3("test");
        });
    }

    @Test
    @DisplayName("유효하지 않은 URL 형식일 경우 FriendyException이 발생한다.")
    void throwsExceptionWhenInvalidUrlFormatIsProvided() {
        //Given
        String invalidUrl = "invalid-url";

        // When & Then
        assertThrows(FriendyException.class, () -> {
            s3service.extractFilePath(invalidUrl);
        });
    }

    @Test
    @DisplayName("S3 객체 복사 실패 시 FriendyException이 발생한다.")
    void throwsFriendyExceptionWhenS3CopyFails() {
        // Given
        String testImageUrl = "https://example.com/images/sample1.jpg";
        String dirname = "test";

        doThrow(new FriendyException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 객체 복사 중 오류 발생"))
                .when(s3Client).copyObject(any(CopyObjectRequest.class));

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3service.moveS3Object(testImageUrl,dirname);
        });

        // Then
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
    }

    @Test
    @DisplayName("IOException 발생 시 FriendyException이 발생한다.")
    void throwsFriendyExceptionWhenIOExceptionOccursDuringUpload() throws IOException {
        // Given
        MultipartFile spyFile = spy(new MockMultipartFile(
                "file", "test-image.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5}
        ));
        String dirName = "test-dir";

        when(spyFile.getInputStream()).thenThrow(new IOException("S3 업로드 실패"));
        doNothing().when(s3exception).validateFile(spyFile);

        // When
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3service.upload(spyFile, dirName);
        });

        // Then
        assertEquals(ErrorCode.FILE_IO_ERROR, exception.getErrorCode());
        assertEquals("S3 업로드 중 오류 발생", exception.getMessage());
    }
}