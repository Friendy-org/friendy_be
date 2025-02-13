package friendy.community.domain.upload.controller;


import friendy.community.infra.storage.s3.service.S3service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class UploadController implements SpringDocUploadController {

    private final S3service s3service;

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadMultipleFile(@RequestPart(value = "file") MultipartFile multipartFile) {
        return s3service.upload(multipartFile,"temp");

    }
}