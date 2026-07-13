package com.srmasset.creditengine.dto.response;

import com.srmasset.creditengine.entity.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        UserRole role,
        boolean enabled,
        OffsetDateTime createdAt
) {
}
