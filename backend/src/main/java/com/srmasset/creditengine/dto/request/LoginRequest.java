package com.srmasset.creditengine.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "usuario e obrigatorio") String username,
        @NotBlank(message = "senha e obrigatoria") String password
) {
}
