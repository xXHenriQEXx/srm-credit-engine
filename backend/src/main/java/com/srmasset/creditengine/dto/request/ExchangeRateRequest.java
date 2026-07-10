package com.srmasset.creditengine.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ExchangeRateRequest(
        @NotBlank @Size(min = 3, max = 3) String baseCurrency,
        @NotBlank @Size(min = 3, max = 3) String quoteCurrency,
        @NotNull @DecimalMin(value = "0.00000001") BigDecimal rate
) {
}
