package com.unihub.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.file.config.FileStorageProperties;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class FileStorageServiceTest {

	private S3Client s3Client;
	private S3Presigner s3Presigner;
	private FileStorageService service;

	@BeforeEach
	void setUp() {
		s3Client = mock(S3Client.class);
		s3Presigner = mock(S3Presigner.class);
		StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
		FileStorageProperties props = new FileStorageProperties("http://localhost:9000", "us-east-1", "key", "secret",
				"unihub-files", 15, 1024 * 1024);
		ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
		service = new FileStorageService(s3Client, s3Presigner, props, redisTemplate, mapper);
	}

	@Test
	void sanitizesFilenameAndGeneratesPresignedUploadUrl() throws Exception {
		PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
		when(presigned.url()).thenReturn(URI.create("http://localhost:9000/unihub-files/test.pdf").toURL());
		when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

		var response = service.createPresignedUpload("Bài giảng tuần 1 (final).pdf", "application/pdf", 2048, "user-1");

		assertThat(response.fileId()).isNotNull();
		assertThat(response.filename()).isEqualTo("Bai_giang_tuan_1__final_.pdf");
		assertThat(response.uploadUrl()).contains("http://localhost:9000");
	}

	@Test
	void rejectsUnsupportedContentTypeAndOversizedFile() {
		assertThatThrownBy(() -> service.createPresignedUpload("script.exe", "application/x-msdownload", 100, "u1"))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.BAD_REQUEST);

		assertThatThrownBy(() -> service.createPresignedUpload("huge.pdf", "application/pdf", 10_000_000L, "u1"))
				.isInstanceOf(AppException.class).extracting(e -> ((AppException) e).getErrorCode())
				.isEqualTo(ErrorCode.BAD_REQUEST);
	}
}
