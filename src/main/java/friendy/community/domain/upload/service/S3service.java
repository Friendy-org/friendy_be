package friendy.community.domain.upload.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import friendy.community.domain.upload.controller.code.UploadExceptionCode;
import friendy.community.domain.upload.dto.response.UploadResponse;
import friendy.community.global.exception.domain.BadGatewayException;
import friendy.community.global.exception.domain.BadRequestException;
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
    private final S3exception s3exception;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public UploadResponse upload(MultipartFile multipartFile, String dirName) {
        s3exception.validateFile(multipartFile);

        String imageUrl = putS3(multipartFile, generateStoredFileName(multipartFile, dirName));

        return new UploadResponse(imageUrl);
    }

    public String generateStoredFileName(MultipartFile multipartFile, String dirName) {
        String originalFileName = multipartFile.getOriginalFilename();

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
            throw new BadRequestException(UploadExceptionCode.INVALID_URL_FORMAT);
        }
    }

    public String getContentTypeFromS3(String key) {
        try {
            S3Object object = s3Client.getObject(new GetObjectRequest(bucket, key));
            return object.getObjectMetadata().getContentType();
        } catch (Exception e) {
            throw new BadGatewayException(UploadExceptionCode.FILE_TYPE_UNAVAILABLE);
        }
    }

    public void deleteFromS3(String s3Key) {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(bucket, s3Key));
        } catch (AmazonS3Exception e) {
            throw new BadGatewayException(UploadExceptionCode.FILE_DELETION_FAILED);
        }
    }

    public String extractS3Key(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String path = url.getPath();
            return path.substring(1);
        } catch (MalformedURLException e) {
            throw new BadRequestException(UploadExceptionCode.INVALID_URL_FORMAT);
        }
    }

    private String putS3(MultipartFile multipartFile, String uuidFileName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            s3Client.putObject(bucket, uuidFileName, inputStream, metadata);
        } catch (IOException e) {
            throw new BadGatewayException(UploadExceptionCode.S3_UPLOAD_ERROR);
        }
        return s3Client.getUrl(bucket, uuidFileName).toString();
    }

    private void copyObject(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucket, sourceKey, destinationBucket, destinationKey);

        try {
            s3Client.copyObject(copyObjectRequest);
        } catch (Exception e) {
            throw new BadGatewayException(UploadExceptionCode.S3_OBJECT_COPY_FAILED);
        }
    }
}