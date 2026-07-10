package com.srmasset.creditengine.repository;

import com.srmasset.creditengine.dto.response.SettlementExtractRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio de leitura para o relatorio "Extrato de Liquidacao".
 *
 * Deliberadamente NAO usa Spring Data JPA / Criteria API: para consultas
 * analiticas que podem varrer grandes volumes de dados com filtros
 * combinados (periodo + cedente + moeda) e paginacao, SQL nativo via
 * JdbcTemplate da controle total sobre os indices utilizados, evita o
 * overhead de hidratacao de entidades JPA (que aqui nao fazem sentido -
 * so precisamos de dados planos para leitura) e facilita o EXPLAIN
 * ANALYZE / tuning no banco quando necessario.
 *
 * Por design (ver README - Arquitetura), esta classe fica em uma
 * "camada de relatorios" de 2 niveis (Controller -> Repository) sem
 * passar pela camada de negocio, pois nao ha regra de negocio aqui,
 * apenas leitura e formatacao de dados ja consolidados.
 */
@Repository
public class SettlementExtractRepository {

    private final JdbcTemplate jdbcTemplate;

    public SettlementExtractRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SettlementExtractRow> ROW_MAPPER = (rs, rowNum) -> new SettlementExtractRow(
            java.util.UUID.fromString(rs.getString("id")),
            rs.getString("assignor_name"),
            rs.getString("receivable_type"),
            rs.getBigDecimal("face_value"),
            rs.getString("face_currency"),
            rs.getString("settlement_currency"),
            rs.getBigDecimal("settlement_value"),
            rs.getString("status"),
            rs.getObject("due_date", LocalDate.class),
            rs.getObject("created_at", java.time.OffsetDateTime.class)
    );

    public List<SettlementExtractRow> findExtract(String assignorName, String settlementCurrency,
                                                    LocalDate from, LocalDate to, int page, int size) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, assignor_name, receivable_type, face_value, face_currency,
                       settlement_currency, settlement_value, status, due_date, created_at
                FROM credit_transaction
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, assignorName, settlementCurrency, from, to);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    public long countExtract(String assignorName, String settlementCurrency, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM credit_transaction WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, assignorName, settlementCurrency, from, to);
        Long result = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return result == null ? 0L : result;
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String assignorName,
                                String settlementCurrency, LocalDate from, LocalDate to) {
        if (assignorName != null && !assignorName.isBlank()) {
            sql.append(" AND assignor_name ILIKE ?");
            params.add("%" + assignorName + "%");
        }
        if (settlementCurrency != null && !settlementCurrency.isBlank()) {
            sql.append(" AND settlement_currency = ?");
            params.add(settlementCurrency.toUpperCase());
        }
        if (from != null) {
            sql.append(" AND created_at >= ?");
            params.add(from.atStartOfDay());
        }
        if (to != null) {
            sql.append(" AND created_at < ?");
            params.add(to.plusDays(1).atStartOfDay());
        }
    }
}
