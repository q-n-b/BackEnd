package qnb.common.dto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //이미지 업로드
    public String upload(MultipartFile file, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 1. 임시 파일로 변환
        File tempFile = File.createTempFile("temp-", file.getOriginalFilename());
        file.transferTo(tempFile);

        // 2. S3에 업로드
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                //.acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(tempFile));

        // 3. 로컬 임시 파일 삭제
        tempFile.delete();

        return getFileUrl(fileName);
    }

    //파일 URL 조회
    private String getFileUrl(String fileName) {
        return "https://" + bucket + ".s3.ap-southeast-2.amazonaws.com/" + fileName;
    }

    //프로필 삭제
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        // S3 Key 추출
        // ex) https://qnb-profile-images.s3.ap-southeast-2.amazonaws.com/user/profile/uuid_img.png
        // -> key = user/profile/uuid_img.png
        String key = fileUrl.substring(fileUrl.indexOf(".com/") + 5);

        // 삭제 요청
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

}
