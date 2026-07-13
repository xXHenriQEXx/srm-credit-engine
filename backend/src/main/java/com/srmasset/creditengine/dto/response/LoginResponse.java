package com.srmasset.creditengine.dto.response;

public record LoginResponse(
        String token,
        String username,
        String role,
        long expiresInSeconds
) {
}
