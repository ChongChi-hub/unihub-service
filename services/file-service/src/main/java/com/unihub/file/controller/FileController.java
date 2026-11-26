package com.unihub.file.controller;

import com.unihub.common.api.ApiResponse;
import com.unihub.file.model.FileMetadata;
import com.unihub.file.service.FileStorageService;
import com.unihub.file.service.FileStorageService.PresignedDownloadResponse;
import com.unihub.file.service.FileStorageService.PresignedUploadResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@PostMapping("/presign-upload")
	public ApiResponse<PresignedUploadResponse> presignUpload(
			@RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
			@Valid @RequestBody PresignUploadRequest request) {
		return ApiResponse.ok(fileStorageService.createPresignedUpload(request.filename(), request.contentType(),
				request.sizeBytes(), userId));
	}

	@PostMapping("/{fileId}/confirm")
	public ApiResponse<FileMetadata> confirm(
			@RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId, @PathVariable UUID fileId) {
		return ApiResponse.ok(fileStorageService.confirmUpload(fileId, userId));
	}

	@GetMapping("/{fileId}/presign-download")
	public ApiResponse<PresignedDownloadResponse> presignDownload(@PathVariable UUID fileId) {
		return ApiResponse.ok(fileStorageService.createPresignedDownload(fileId));
	}

	@DeleteMapping("/{fileId}")
	public ApiResponse<Void> deleteFile(@RequestHeader(value = "X-User-Id", defaultValue = "anonymous") String userId,
			@RequestHeader(value = "X-User-Roles", defaultValue = "") String roles, @PathVariable UUID fileId) {
		boolean isAdmin = roles.contains("ADMIN");
		fileStorageService.deleteFile(fileId, userId, isAdmin);
		return ApiResponse.okMessage("File deleted");
	}

	public record PresignUploadRequest(@NotBlank String filename, @NotBlank String contentType,
			@Min(1) long sizeBytes) {
	}
}
