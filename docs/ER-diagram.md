# Diagrama Entidade-Relacionamento — SRM Credit Engine

```mermaid
erDiagram
    CURRENCY ||--o{ EXCHANGE_RATE : "base_currency"
    CURRENCY ||--o{ EXCHANGE_RATE : "quote_currency"
    CURRENCY ||--o{ CREDIT_TRANSACTION : "face_currency"
    CURRENCY ||--o{ CREDIT_TRANSACTION : "settlement_currency"

    CURRENCY {
        varchar code PK "ISO 4217 (ex BRL, USD)"
        varchar name
        varchar symbol
    }

    EXCHANGE_RATE {
        bigint id PK
        varchar base_currency FK
        varchar quote_currency FK
        numeric rate
        timestamptz effective_at
    }

    CREDIT_TRANSACTION {
        uuid id PK
        varchar assignor_name "cedente"
        varchar receivable_type "DUPLICATA_MERCANTIL | CHEQUE_PRE_DATADO"
        numeric face_value
        varchar face_currency FK
        varchar settlement_currency FK
        date due_date
        numeric term_months
        numeric spread_applied
        numeric base_rate_applied
        numeric exchange_rate_applied
        numeric settlement_value
        varchar status "PENDING | SETTLED | CANCELLED"
        timestamptz created_at
        bigint version "optimistic locking"
    }
```

## Decisões de modelagem

- **`currency.code` como PK natural (não surrogate key)**: o conjunto de moedas é pequeno, estável e o código ISO já é um identificador único e legível por natureza — um `id` auto-incremento só adicionaria uma junção sem trazer valor.
- **`exchange_rate` é append-only (nunca UPDATE)**: cada atualização de taxa gera uma nova linha com `effective_at = now()`. Isso preserva o histórico de taxas para auditoria — uma transação liquidada há 3 meses sempre pode ser conferida com a taxa exata usada naquele momento, mesmo que a taxa atual seja outra.
- **`credit_transaction` desnormalizada de propósito**: guardamos `spread_applied`, `base_rate_applied` e `exchange_rate_applied` MESMO sabendo que eles já existem/existiram em outras tabelas. Em um motor financeiro, o registro de uma transação liquidada precisa ser imutável e autossuficiente — se a estratégia de spread mudar amanhã (ex: duplicata passa de 1.5% para 1.8%), os registros antigos não podem "mudar de valor" silenciosamente.
- **`version` (optimistic locking)**: protege contra condição de corrida em liquidações concorrentes sobre o mesmo registro, conforme requisito de item Sênior "Concorrência" — implementado aqui via `@Version` do JPA por ser de baixo custo e não exigir infraestrutura adicional.
