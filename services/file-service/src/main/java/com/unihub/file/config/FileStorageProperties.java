package com.unihub.file.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record FileStorageProperties(String endpoint, String region, String accessKey, String secretKey, String bucket,
		long presignExpirationMinutes, long maxFileSizeBytes) {
}
