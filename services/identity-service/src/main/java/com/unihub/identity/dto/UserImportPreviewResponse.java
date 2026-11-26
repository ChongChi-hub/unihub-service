package com.unihub.identity.dto;

import java.util.List;
import java.util.Set;

public record UserImportPreviewResponse(int totalRows, int validCount, int errorCount, List<ImportRow> validRows,
		List<RowError> errors) {

	public record ImportRow(int rowNumber, String email, String fullName, String code, Set<String> roles) {
	}

	public record RowError(int rowNumber, String email, String message) {
	}
}
