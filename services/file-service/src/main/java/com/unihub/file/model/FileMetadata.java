package com.unihub.file.model;

import java.time.Instant;
import java.util.UUID;

public record FileMetadata(UUID fileId, String objectKey, String originalFilename, String contentType, long sizeBytes,
		String ownerUserId, boolean confirmed, Instant createdAt) {
}
