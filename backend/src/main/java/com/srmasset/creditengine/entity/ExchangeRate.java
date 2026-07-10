package com.srmasset.creditengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Taxa de cambio entre duas moedas em um determinado instante.
 * Mantemos historico (nunca fazemos update na taxa) para que
 * transacoes ja liquidadas sempre possam ser auditadas com a
 * taxa exata utilizada no momento da operacao.
 */
@Entity
@Table(name = "exchange_rate", indexes = {
        @Index(name = "idx_exchange_rate_pair", columnList = "base_currency,quote_currency,effective_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "base_currency", nullable = false)
    private Currency baseCurrency;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_currency", nullable = false)
    private Currency quoteCurrency;

    /** Quantidade de quoteCurrency equivalente a 1 unidade de baseCurrency. */
    @Column(name = "rate", nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;

    @Column(name = "effective_at", nullable = false)
    private OffsetDateTime effectiveAt;
}
