
package com.dual.dev.account_service.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3StorageProperties.class)
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(S3StorageProperties properties) {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        properties.accessKey(),
                        properties.secretKey()
                );

        return S3Client.builder()
                .endpointOverride(
                        URI.create(properties.endpoint())
                )
                .region(
                        Region.of(properties.region())
                )
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .forcePathStyle(true)
                .build();
    }
}
