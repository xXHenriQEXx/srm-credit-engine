package com.srmasset.creditengine.dto.response;

import java.math.BigDecimal;

/**
 * Resultado do calculo de precificacao. Usado tanto para a simulacao
 * (nao persiste nada) quanto retornado apos a criacao efetiva da transacao.
 */
public record PricingResultResponse(
        BigDecimal faceValue,
        String faceCurrency,
        BigDecimal termMonths,
        BigDecimal baseRateApplied,
        BigDecimal spreadApplied,
        BigDecimal presentValueInFaceCurrency,
        BigDecimal exchangeRateApplied,
        String settlementCurrency,
        BigDecimal settlementValue
) {
}
