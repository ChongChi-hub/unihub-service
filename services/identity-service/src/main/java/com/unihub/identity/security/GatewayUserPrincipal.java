package com.unihub.identity.security;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated user principal extracted from gateway headers or Bearer JWT.
 */
public record GatewayUserPrincipal(UUID userId, String email, List<String> roles) {
}
