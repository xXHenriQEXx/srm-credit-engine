package com.srmasset.creditengine.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa a cessao/liquidacao de um unico recebivel.
 * Guardamos tanto o valor de face (moeda original do titulo) quanto o
 * valor presente calculado e a moeda efetivamente liquidada, permitindo
 * operacoes cross-currency (titulo emitido em BRL, pago em USD, por exemplo).
 *
 * Toda a persistencia desta entidade acontece dentro de uma unica
 * transacao ACID (ver TransactionService), garantindo que o registro
 * nunca fique "pela metade".
 */
@Entity
@Table(name = "credit_transaction", indexes = {
        @Index(name = "idx_transaction_assignor", columnList = "assignor_name"),
        @Index(name = "idx_transaction_created_at", columnList = "created_at"),
        @Index(name = "idx_transaction_settlement_currency", columnList = "settlement_currency")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Empresa cedente do titulo. */
    @Column(name = "assignor_name", nullable = false, length = 150)
    private String assignorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "receivable_type", nullable = false, length = 40)
    private ReceivableType receivableType;

    @Column(name = "face_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal faceValue;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "face_currency", nullable = false)
    private Currency faceCurrency;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_currency", nullable = false)
    private Currency settlementCurrency;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Prazo em meses usado no calculo de deságio (dias/30, arredondado). */
    @Column(name = "term_months", nullable = false, precision = 10, scale = 4)
    private BigDecimal termMonths;

    @Column(name = "spread_applied", nullable = false, precision = 8, scale = 6)
    private BigDecimal spreadApplied;

    @Column(name = "base_rate_applied", nullable = false, precision = 8, scale = 6)
    private BigDecimal baseRateApplied;

    /** Taxa de cambio usada na conversao, nula se operacao for na mesma moeda. */
    @Column(name = "exchange_rate_applied", precision = 18, scale = 8)
    private BigDecimal exchangeRateApplied;

    /** Valor presente ja na moeda de liquidacao (o que sera efetivamente pago ao cedente). */
    @Column(name = "settlement_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal settlementValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Username do operador que registrou esta liquidacao (auditoria). */
    @Column(name = "created_by", nullable = false, length = 60)
    private String createdBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version; // otimistic locking - evita race condition em liquidacao concorrente
}
