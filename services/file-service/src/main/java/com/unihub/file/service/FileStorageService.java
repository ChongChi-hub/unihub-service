package com.unihub.file.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import com.unihub.file.config.FileStorageProperties;
import com.unihub.file.model.FileMetadata;
import jakarta.annotation.PostConstruct;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * Manages S3/MinIO presigned upload/download URLs, validation, and orphan
 * cleanup.
 */
@Service
public class FileStorageService {

	private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
	private static final String REDIS_PREFIX = "file:meta:";
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf", "image/jpeg", "image/png",
			"image/webp", "text/plain", "text/csv", "application/zip", "video/mp4",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final FileStorageProperties props;
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;
	private final Map<UUID, FileMetadata> localFallbackStore = new ConcurrentHashMap<>();

	public FileStorageService(S3Client s3Client, S3Presigner s3Presigner, FileStorageProperties props,
			StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.props = props;
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void ensureBucketExists() {
		try {
			s3Client.headBucket(HeadBucketRequest.builder().bucket(props.bucket()).build());
		} catch (NoSuchBucketException ex) {
			s3Client.createBucket(CreateBucketRequest.builder().bucket(props.bucket()).build());
			log.info("Created S3 bucket: {}", props.bucket());
		} catch (Exception ex) {
			log.warn("Could not verify S3 bucket {} on startup: {}", props.bucket(), ex.getMessage());
		}
	}

	public PresignedUploadResponse createPresignedUpload(String filename, String contentType, long sizeBytes,
			String ownerUserId) {
		validateUpload(filename, contentType, sizeBytes);

		UUID fileId = UUID.randomUUID();
		String safeName = sanitizeFilename(filename);
		String objectKey = ownerUserId + "/" + fileId + "-" + safeName;

		PutObjectRequest putRequest = PutObjectRequest.builder().bucket(props.bucket()).key(objectKey)
				.contentType(contentType).build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(props.presignExpirationMinutes())).putObjectRequest(putRequest)
				.build();

		String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
		FileMetadata metadata = new FileMetadata(fileId, objectKey, safeName, contentType, sizeBytes, ownerUserId,
				false, Instant.now());
		saveMetadata(metadata);

		return new PresignedUploadResponse(fileId, objectKey, uploadUrl, safeName);
	}

	public PresignedDownloadResponse createPresignedDownload(UUID fileId) {
		FileMetadata metadata = loadMetadata(fileId);
		GetObjectRequest getRequest = GetObjectRequest.builder().bucket(props.bucket()).key(metadata.objectKey())
				.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(props.presignExpirationMinutes())).getObjectRequest(getRequest)
				.build();

		String downloadUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
		return new PresignedDownloadResponse(fileId, metadata.originalFilename(), metadata.contentType(), downloadUrl);
	}

	public FileMetadata confirmUpload(UUID fileId, String ownerUserId) {
		FileMetadata existing = loadMetadata(fileId);
		if (!existing.ownerUserId().equals(ownerUserId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Not allowed to confirm this file");
		}
		FileMetadata confirmed = new FileMetadata(existing.fileId(), existing.objectKey(), existing.originalFilename(),
				existing.contentType(), existing.sizeBytes(), existing.ownerUserId(), true, existing.createdAt());
		saveMetadata(confirmed);
		return confirmed;
	}

	public void deleteFile(UUID fileId, String ownerUserId, boolean isAdmin) {
		FileMetadata existing = loadMetadata(fileId);
		if (!isAdmin && !existing.ownerUserId().equals(ownerUserId)) {
			throw new AppException(ErrorCode.FORBIDDEN, "Not allowed to delete this file");
		}
		s3Client.deleteObject(DeleteObjectRequest.builder().bucket(props.bucket()).key(existing.objectKey()).build());
		localFallbackStore.remove(fileId);
		try {
			redisTemplate.delete(REDIS_PREFIX + fileId);
		} catch (Exception ignored) {
		}
	}

	@Scheduled(fixedDelay = 3600_000L)
	public void cleanupUnconfirmedOrphans() {
		Instant cutoff = Instant.now().minus(Duration.ofHours(24));
		localFallbackStore.values().stream().filter(meta -> !meta.confirmed() && meta.createdAt().isBefore(cutoff))
				.forEach(meta -> {
					try {
						s3Client.deleteObject(
								DeleteObjectRequest.builder().bucket(props.bucket()).key(meta.objectKey()).build());
						localFallbackStore.remove(meta.fileId());
						log.info("Cleaned up orphan file {}", meta.fileId());
					} catch (Exception ex) {
						log.warn("Failed orphan cleanup for {}: {}", meta.fileId(), ex.getMessage());
					}
				});
	}

	void validateUpload(String filename, String contentType, long sizeBytes) {
		if (!StringUtils.hasText(filename)) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Filename is required");
		}
		if (sizeBytes <= 0 || sizeBytes > props.maxFileSizeBytes()) {
			throw new AppException(ErrorCode.BAD_REQUEST,
					"File size must be between 1 byte and " + props.maxFileSizeBytes() + " bytes");
		}
		if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
			throw new AppException(ErrorCode.BAD_REQUEST, "Unsupported file type: " + contentType);
		}
	}

	static String sanitizeFilename(String filename) {
		String normalized = Normalizer.normalize(filename.trim(), Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
		return normalized.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	private void saveMetadata(FileMetadata metadata) {
		localFallbackStore.put(metadata.fileId(), metadata);
		try {
			redisTemplate.opsForValue().set(REDIS_PREFIX + metadata.fileId(),
					objectMapper.writeValueAsString(metadata));
		} catch (Exception ex) {
			log.warn("Redis unavailable for file metadata save: {}", ex.getMessage());
		}
	}

	private FileMetadata loadMetadata(UUID fileId) {
		try {
			String json = redisTemplate.opsForValue().get(REDIS_PREFIX + fileId);
			if (json != null) {
				return objectMapper.readValue(json, FileMetadata.class);
			}
		} catch (Exception ignored) {
		}
		FileMetadata local = localFallbackStore.get(fileId);
		if (local == null) {
			throw new AppException(ErrorCode.NOT_FOUND, "File not found: " + fileId);
		}
		return local;
	}

	public record PresignedUploadResponse(UUID fileId, String objectKey, String uploadUrl, String filename) {
	}

	public record PresignedDownloadResponse(UUID fileId, String filename, String contentType, String downloadUrl) {
	}
}
