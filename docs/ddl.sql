-- V1__init_schema.sql
-- SRM Credit Engine - schema inicial

CREATE TABLE currency (
    code       VARCHAR(3) PRIMARY KEY,
    name       VARCHAR(60) NOT NULL,
    symbol     VARCHAR(5)
);

CREATE TABLE exchange_rate (
    id               BIGSERIAL PRIMARY KEY,
    base_currency    VARCHAR(3) NOT NULL REFERENCES currency(code),
    quote_currency   VARCHAR(3) NOT NULL REFERENCES currency(code),
    rate             NUMERIC(18,8) NOT NULL CHECK (rate > 0),
    effective_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_exchange_rate_pair ON exchange_rate (base_currency, quote_currency, effective_at DESC);

CREATE TABLE credit_transaction (
    id                      UUID PRIMARY KEY,
    assignor_name           VARCHAR(150) NOT NULL,
    receivable_type         VARCHAR(40)  NOT NULL,
    face_value              NUMERIC(18,2) NOT NULL CHECK (face_value > 0),
    face_currency           VARCHAR(3) NOT NULL REFERENCES currency(code),
    settlement_currency     VARCHAR(3) NOT NULL REFERENCES currency(code),
    due_date                DATE NOT NULL,
    term_months             NUMERIC(10,4) NOT NULL,
    spread_applied          NUMERIC(8,6) NOT NULL,
    base_rate_applied       NUMERIC(8,6) NOT NULL,
    exchange_rate_applied   NUMERIC(18,8),
    settlement_value        NUMERIC(18,2) NOT NULL CHECK (settlement_value > 0),
    status                  VARCHAR(20) NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL,
    version                 BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_transaction_assignor ON credit_transaction (assignor_name);
CREATE INDEX idx_transaction_created_at ON credit_transaction (created_at DESC);
CREATE INDEX idx_transaction_settlement_currency ON credit_transaction (settlement_currency);

-- Seed basico de moedas e taxa inicial USD/BRL
INSERT INTO currency (code, name, symbol) VALUES
    ('BRL', 'Real Brasileiro', 'R$'),
    ('USD', 'Dolar Americano', '$');

INSERT INTO exchange_rate (base_currency, quote_currency, rate, effective_at) VALUES
    ('USD', 'BRL', 5.40000000, now()),
    ('BRL', 'USD', 0.18518519, now());
