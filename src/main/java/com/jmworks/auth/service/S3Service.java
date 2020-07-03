package com.jmworks.auth.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// upload path : /bucket-download
// 참고 : https://docs.aws.amazon.com/ko_kr/sdk-for-java/v1/developer-guide/examples-s3-objects.html#upload-object
// https://charlie-choi.tistory.com/236
@Service
public class S3Service {
    @Value("${s3.bucket}")
    String bucket;

    @Value("${s3.endpoint}")
    String s3URL;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file, String toFileName) throws IOException {
        String accessURL = "";
        String extention = "";

        extention = file.getOriginalFilename().split("\\.")[1];

        ObjectMetadata omd = new ObjectMetadata();

        omd.setContentType(file.getContentType());
        omd.setContentLength(file.getSize());
        omd.setHeader("filename", file.getOriginalFilename());

        s3Client.putObject( new PutObjectRequest( bucket, toFileName + "." + extention , file.getInputStream(), omd));

        accessURL = s3URL + "/" + bucket + "/" + toFileName + "." + extention;
        System.out.println( accessURL );

        return accessURL;
    }
}
