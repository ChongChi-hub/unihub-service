package com.unihub.file.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	@Bean
	public S3Client s3Client(FileStorageProperties props) {
		return S3Client.builder().endpointOverride(URI.create(props.endpoint())).region(Region.of(props.region()))
				.credentialsProvider(StaticCredentialsProvider
						.create(AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();
	}

	@Bean
	public S3Presigner s3Presigner(FileStorageProperties props) {
		return S3Presigner.builder().endpointOverride(URI.create(props.endpoint())).region(Region.of(props.region()))
				.credentialsProvider(StaticCredentialsProvider
						.create(AwsBasicCredentials.create(props.accessKey(), props.secretKey())))
				.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build();
	}
}
