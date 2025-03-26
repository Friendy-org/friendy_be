package friendy.community.domain.upload.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import friendy.community.domain.upload.controller.code.UploadExceptionCode;
import friendy.community.domain.upload.dto.response.UploadResponse;
import friendy.community.global.exception.domain.BadGatewayException;
import friendy.community.global.exception.domain.BadRequestException;
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
import software.amazon.awssdk.core.exception.SdkClientException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@DirtiesContext
class S3serviceTest {

    @Mock
    MultipartFile multipartFile;
    @Autowired
    private S3service s3Service;
    @MockitoBean
    private AmazonS3 s3Client;
    @Mock
    private S3exception s3exception;

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
        UploadResponse uploadResponse = s3Service.upload(multipartFile, "test-dir");

        // Then
        assertThat(uploadResponse.imageUrl()).isEqualTo(tempUrl);
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
        String actualUrl = s3Service.moveS3Object(imageUrl, newDirName);

        // Then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(s3Client).copyObject(any());
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
        String result = s3Service.getContentTypeFromS3("test");

        // Then
        assertThat(result).isEqualTo("image/png");
    }

    @Test
    @DisplayName("정상적인 파일 타입을 S3에서 가져오지 못하면 예외가 발생한다.")
    void throwsExceptionWhenGetContentTypeFromS3Fails() {
        // Given
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(SdkClientException.class);

        // When & Then
        assertThrows(BadGatewayException.class, () -> {
            s3Service.getContentTypeFromS3("test");
        });
    }

    @Test
    @DisplayName("유효하지 않은 URL 형식일 경우 FriendyException이 발생한다.")
    void throwsExceptionWhenInvalidUrlFormatIsProvided() {
        //Given
        String invalidUrl = "invalid-url";

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            s3Service.extractFilePath(invalidUrl);
        });
    }

    @Test
    @DisplayName("S3 객체 복사 실패 시 FriendyException이 발생한다.")
    void throwsFriendyExceptionWhenS3CopyFails() {
        // Given
        String testImageUrl = "https://example.com/images/sample1.jpg";
        String dirname = "test";

        when(s3Client.copyObject(any(CopyObjectRequest.class)))
            .thenThrow(SdkClientException.class);

        // When
        assertThatThrownBy(() ->
            s3Service.moveS3Object(testImageUrl, dirname))
            .isInstanceOf(BadGatewayException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.S3_OBJECT_COPY_FAILED);
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

        // when & then
        assertThatThrownBy(() ->
            s3Service.upload(spyFile, dirName))
            .isInstanceOf(BadGatewayException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.S3_UPLOAD_ERROR);
    }

    @Test
    @DisplayName("파일 삭제 실패 시 FriendyException을 던진다")
    void throwsFriendyExceptionWhenDeleteFromS3Fails() {
        // Given
        String s3Key = "post/image.jpg";
        doThrow(AmazonS3Exception.class).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // when & then
        assertThatThrownBy(() -> s3Service.deleteFromS3(s3Key))
            .isInstanceOf(BadGatewayException.class)
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.FILE_DELETION_FAILED);
    }

    @Test
    @DisplayName("S3 URL에서 객체 키를 정상적으로 추출한다")
    void extractS3KeySuccess() {
        // Given
        String imageUrl = "https://test.s3.us-east-2.amazonaws.com/test/1da368de-8ad8-4c0a-bd12-d51ad6c4e937.png";

        // When
        String s3Key = s3Service.extractS3Key(imageUrl);

        // Then
        assertThat(s3Key).isEqualTo("test/1da368de-8ad8-4c0a-bd12-d51ad6c4e937.png");
    }

    @Test
    @DisplayName("잘못된 URL 형식일 경우 예외가 발생한다")
    void extractS3KeyInvalidUrl() {
        // Given
        String invalidUrl = "invalid-url";

        // When & Then
        assertThatThrownBy(() -> s3Service.extractS3Key(invalidUrl))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("유효한 URL 형식이어야 합니다.")
            .hasFieldOrPropertyWithValue("exceptionType", UploadExceptionCode.INVALID_URL_FORMAT);
    }

    @Test
    @DisplayName("S3에서 파일 삭제가 성공한다.")
    void deleteFileFromS3Successfully() {
        // given
        String s3Key = "someFileKey";
        doNothing().when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // when & then
        assertThatCode(() -> s3Service.deleteFromS3(s3Key)).doesNotThrowAnyException();
    }

}