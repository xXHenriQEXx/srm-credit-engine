package com.srmasset.creditengine.dto.response;

import com.srmasset.creditengine.entity.ReceivableType;
import com.srmasset.creditengine.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String assignorName,
        ReceivableType receivableType,
        BigDecimal faceValue,
        String faceCurrency,
        String settlementCurrency,
        LocalDate dueDate,
        BigDecimal settlementValue,
        TransactionStatus status,
        OffsetDateTime createdAt
) {
}
