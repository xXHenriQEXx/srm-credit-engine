-- V3__add_created_by_to_transaction.sql
-- Rastreabilidade: identifica qual operador registrou cada liquidacao.
-- DEFAULT 'system' para retrocompatibilidade com registros pre-existentes.

ALTER TABLE credit_transaction
    ADD COLUMN created_by VARCHAR(60) NOT NULL DEFAULT 'system';

CREATE INDEX idx_transaction_created_by ON credit_transaction (created_by);
