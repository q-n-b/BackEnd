package qnb.common;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import qnb.common.dto.S3Uploader;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class TestUploadController {

    private final S3Uploader s3Uploader;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        String url = s3Uploader.upload(file, "profiles");
        return ResponseEntity.ok(url);
    }
}
