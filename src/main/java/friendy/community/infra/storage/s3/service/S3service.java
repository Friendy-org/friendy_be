package friendy.community.infra.storage.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import friendy.community.domain.upload.dto.response.UploadResponse;
import friendy.community.global.exception.ErrorCode;
import friendy.community.infra.storage.s3.exception.S3exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3service {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3exception s3exception;

    public UploadResponse upload(MultipartFile multipartFile, String dirName) {
        s3exception.validateFile(multipartFile);

        String imageUrl = putS3(multipartFile, generateStoredFileName(multipartFile, dirName));

        return new UploadResponse(imageUrl);
    }

    public String generateStoredFileName(MultipartFile multipartFile, String dirName) {
        String originalFileName = multipartFile.getOriginalFilename();

        if (originalFileName == null) {
            throw new FriendyException(ErrorCode.INVALID_FILE ,"파일 이름이 없습니다.");
        }
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return dirName + "/" + uuid + extension;
    }

    public String moveS3Object(String imageUrl, String newDirName) {
        String oldKey = extractFilePath(imageUrl);
        String fileName = oldKey.substring(oldKey.lastIndexOf("/") + 1);
        String newKey = newDirName + "/" + fileName;

        copyObject(bucket, oldKey, bucket, newKey);

        return s3Client.getUrl(bucket, newKey).toString();
    }

    public String extractFilePath(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            return path.substring(1);
        } catch (MalformedURLException e) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "유효한 URL 형식이어야 합니다.");
        }
    }

    public String getContentTypeFromS3(String key) {
        try {
            S3Object object = s3Client.getObject(new GetObjectRequest(bucket, key));
            return object.getObjectMetadata().getContentType();
        } catch (Exception e) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "파일타입을 가져올수 없습니다.");
        }
    }

    public void deleteFromS3(String s3Key) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucket, s3Key));
        }catch (AmazonS3Exception e) {
            throw new FriendyException(ErrorCode.INTERNAL_SERVER_ERROR, "파일을 삭제하지 못햇습니다.");
        }
    }

    public String extractS3Key(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            return path.substring(1);
        } catch (MalformedURLException e) {
            throw new FriendyException(ErrorCode.INVALID_FILE, "유효한 URL 형식이어야 합니다.");
        }
    }

    private String putS3(MultipartFile multipartFile, String uuidFileName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            s3Client.putObject(bucket, uuidFileName, inputStream, metadata);
        } catch (IOException e) {
            throw new FriendyException(ErrorCode.FILE_IO_ERROR, "S3 업로드 중 오류 발생");
        }
        return s3Client.getUrl(bucket, uuidFileName).toString();
    }

    private void copyObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
            sourceBucket, sourceKey,
            destinationBucket, destinationKey);

        try {
            s3Client.copyObject(copyObjectRequest);
        } catch (FriendyException e) {
            throw new FriendyException(ErrorCode.INTERNAL_SERVER_ERROR,"S3 객체 복사에 실패했습니다");
        }
    }
}