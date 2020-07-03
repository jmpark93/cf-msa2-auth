package com.jmworks.auth.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${s3.endpoint}")
    String s3URL;

    @Value("${s3.accessKey}")
    String accessKey;

    @Value("${s3.secretKey}")
    String secretKey;


    @Bean
    public BasicAWSCredentials S3Credentials() {
        BasicAWSCredentials s3Credentials = new BasicAWSCredentials(accessKey, secretKey);
        return s3Credentials;
    }

    @Bean
    public AmazonS3 S3Client() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");

        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3URL, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(this.S3Credentials()))
                .build();

        return s3Client;
    }
}
