package com.unihub.academic.client;

import com.unihub.common.api.ApiResponse;
import com.unihub.common.exception.AppException;
import com.unihub.common.exception.ErrorCode;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Controlled Resilience4j fallback when identity-service is slow or
 * unreachable.
 */
@Component
public class IdentityClientFallback implements IdentityClient {

	private static final Logger log = LoggerFactory.getLogger(IdentityClientFallback.class);

	@Override
	public ApiResponse<RemoteUserDto> getUserById(UUID id) {
		log.warn("Fallback triggered for identity-service getUserById({})", id);
		throw new AppException(ErrorCode.INTERNAL_ERROR, "Identity service is temporarily unavailable");
	}
}
