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
    private S3service s3service;  // 실제 테스트 대상 클래스

    @MockitoBean
    private AmazonS3 s3Client;  // AmazonS3 mock 객체

    @Mock
    private S3exception s3exception;  // S3exception mock 객체

    @Mock
    private File file;

    @Mock
    MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Mockito 초기화
    }

    @Test
    void upload_성공() throws MalformedURLException {
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
    void moveS3Object_성공() throws MalformedURLException {
        // Given
        String imageUrl = "https://your-bucket.s3.amazonaws.com/old-dir/test.jpg";
        String newDirName = "new-dir";
        String expectedUrl = "https://your-bucket.s3.amazonaws.com/new-dir/test.jpg";

        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL(expectedUrl));

        // When
        String actualUrl = s3service.moveS3Object(imageUrl, newDirName);

        // Then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(s3Client).copyObject(any()); // copyObject 호출 확인
    }

    @Test
    void testGenerateStoredFileName_FileNameNotFoundException() {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        assertThrows(FriendyException.class, () -> {
            s3service.generateStoredFileName(multipartFile,"upload");
        }, "파일 이름을 가져올 수 없습니다.");

    }

    @Test
    void getContentTypeFromS3_정상적인_파일타입_가져오기() {
        // given
        S3Object s3Object = new S3Object();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");
        s3Object.setObjectMetadata(metadata);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
        // when
        String result = s3service.getContentTypeFromS3("test");

        // then
        assertThat(result).isEqualTo("image/png");
    }

    @Test
    void getContentTypeFromS3_정상적인_파일타입_가져오지못함() {
        // given

        when(s3Client.getObject(any(GetObjectRequest.class))).
                thenThrow(new FriendyException(ErrorCode.INVALID_FILE,"파일타입을 가져올수 없습니다."));

        assertThrows(FriendyException.class, () -> {
            s3service.getContentTypeFromS3("test");
        });
    }

    @Test
    void extractFilePath_유효하지않은URL형식_FriendyException발생() {
        String invalidUrl = "invalid-url";

        assertThrows(FriendyException.class, () -> {
            s3service.extractFilePath(invalidUrl);
        });
    }

    @Test
    void copyObject_ThrowsFriendyException_WhenS3CopyFails() {
        // given
        String testImageUrl = "https://example.com/images/sample1.jpg";
        String dirname = "test";

        // S3Client의 copyObject 메서드가 예외를 던지도록 설정
        doThrow(new FriendyException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 객체 복사 중 오류 발생"))
                .when(s3Client).copyObject(any(CopyObjectRequest.class));

        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3service.moveS3Object(testImageUrl,dirname);
        });
        // when & then

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());

    }

    @Test
    void upload_ThrowsFriendyException_WhenIOExceptionOccurs() throws IOException {
        // given
        MultipartFile spyFile = spy(new MockMultipartFile(
                "file", "test-image.jpg", "image/jpeg", new byte[]{1, 2, 3, 4, 5}
        ));
        String dirName = "test-dir";

        when(spyFile.getInputStream()).thenThrow(new IOException("S3 업로드 실패"));
        doNothing().when(s3exception).validateFile(spyFile);

        // when & then
        FriendyException exception = assertThrows(FriendyException.class, () -> {
            s3service.upload(spyFile, dirName);
        });

        assertEquals(ErrorCode.FILE_IO_ERROR, exception.getErrorCode());
        assertEquals("S3 업로드 중 오류 발생", exception.getMessage());
    }
}