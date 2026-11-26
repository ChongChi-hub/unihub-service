package com.unihub.common.api;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Consistent pagination wrapper decoupled from Spring's internal Page JSON
 * structure.
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages, boolean hasNext) {

	public static <T> PageResponse<T> from(Page<T> springPage) {
		return new PageResponse<>(springPage.getContent(), springPage.getNumber(), springPage.getSize(),
				springPage.getTotalElements(), springPage.getTotalPages(), springPage.hasNext());
	}
}
