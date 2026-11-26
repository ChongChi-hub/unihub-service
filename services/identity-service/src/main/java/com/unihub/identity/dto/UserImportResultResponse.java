package com.unihub.identity.dto;

import java.util.List;

public record UserImportResultResponse(int totalRows, int importedCount, int failedCount,
		List<UserImportPreviewResponse.RowError> errors) {
}
