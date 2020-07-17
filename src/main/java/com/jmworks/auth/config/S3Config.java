package com.jmworks.auth.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
        ClientConfiguration clientConfiguration = ignoringInvalidSslCertificates(new ClientConfiguration());
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

    private ClientConfiguration ignoringInvalidSslCertificates(
            final ClientConfiguration clientConfiguration) {

        clientConfiguration.getApacheHttpClientConfig()
                .withSslSocketFactory(new SSLConnectionSocketFactory(
                        createBlindlyTrustingSSLContext(),
                        NoopHostnameVerifier.INSTANCE));

        return clientConfiguration;
    }

    private SSLContext createBlindlyTrustingSSLContext() {
        try {
            final SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, new TrustManager[]{new X509ExtendedTrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                    // no-op
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                    // no-op
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] arg0, final String arg1,
                                               final Socket arg2)
                        throws CertificateException {
                    // no-op
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] arg0, final String arg1,
                                               final SSLEngine arg2)
                        throws CertificateException {
                    // no-op
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] arg0, final String arg1,
                                               final Socket arg2)
                        throws CertificateException {
                    // no-op
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] arg0, final String arg1,
                                               final SSLEngine arg2)
                        throws CertificateException {
                    // no-op
                }
            }
            }, new java.security.SecureRandom());

            return sc;
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
