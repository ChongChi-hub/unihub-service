package com.unihub.academic.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityClientFallbackTest {

	@Test
	void fallbackThrowsControlledAppException() {
		IdentityClientFallback fallback = new IdentityClientFallback();

		assertThatThrownBy(() -> fallback.getUserById(UUID.randomUUID())).isInstanceOf(AppException.class)
				.extracting(e -> ((AppException) e).getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
	}
}
