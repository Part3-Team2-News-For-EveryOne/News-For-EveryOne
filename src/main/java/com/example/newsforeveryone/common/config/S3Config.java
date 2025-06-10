package com.example.newsforeveryone.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@Configuration
public class S3Config {

    @Value("${newsforeveryone.storage.s3.access-key}")
    private String accessKey;

    @Value("${newsforeveryone.storage.s3.secret-key}")
    private String secretKey;

    @Value("${newsforeveryone.storage.s3.region}")
    private String region;


    // 실제 파일을 s3에 업로드/다운로드 하기 위한 클라이언트
    @Bean
    public software.amazon.awssdk.services.s3.S3Client S3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return software.amazon.awssdk.services.s3.S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
