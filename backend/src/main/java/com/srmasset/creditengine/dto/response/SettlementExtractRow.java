package com.srmasset.creditengine.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Linha do relatorio "Extrato de Liquidacao". E um DTO plano (nao um
 * agregado JPA) porque este relatorio e alimentado por SQL nativo
 * (ver SettlementExtractRepository) - propositalmente fora do ORM
 * para performance em consultas analiticas de grande volume.
 */
public record SettlementExtractRow(
        UUID id,
        String assignorName,
        String receivableType,
        BigDecimal faceValue,
        String faceCurrency,
        String settlementCurrency,
        BigDecimal settlementValue,
        String status,
        LocalDate dueDate,
        OffsetDateTime createdAt,
        String createdBy
) {
}
