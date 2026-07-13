package com.srmasset.creditengine.dto.request;

import com.srmasset.creditengine.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 60, message = "usuario deve ter entre 3 e 60 caracteres") String username,
        @NotBlank @Size(min = 6, message = "senha deve ter no minimo 6 caracteres") String password,
        @NotNull(message = "papel (role) e obrigatorio") UserRole role
) {
}
