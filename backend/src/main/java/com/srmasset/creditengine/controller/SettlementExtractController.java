package com.srmasset.creditengine.controller;

import com.srmasset.creditengine.dto.response.PagedResult;
import com.srmasset.creditengine.dto.response.SettlementExtractRow;
import com.srmasset.creditengine.repository.SettlementExtractRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Rota de "Extrato de Liquidacao" (consulta analitica).
 *
 * Nota de arquitetura: este controller fala DIRETO com o
 * SettlementExtractRepository (SQL nativo via JdbcTemplate), sem passar
 * por uma camada de Service de negocio - conforme especificado no
 * escopo tecnico, relatorios podem ter apenas 2 camadas (apresentacao +
 * persistencia) pois nao ha regra de negocio a ser aplicada, apenas
 * filtragem/paginacao de dados ja consolidados.
 */
@RestController
@RequestMapping("/api/v1/reports/settlement-extract")
@Tag(name = "Reports", description = "Consultas analiticas (extrato de liquidacao)")
public class SettlementExtractController {

    private final SettlementExtractRepository repository;

    public SettlementExtractController(SettlementExtractRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Extrato de liquidacao filtravel por cedente, moeda, periodo e operador, com paginacao server-side")
    public ResponseEntity<PagedResult<SettlementExtractRow>> getExtract(
            @Parameter(description = "Filtro parcial pelo nome do cedente") @RequestParam(required = false) String assignorName,
            @Parameter(description = "Codigo ISO da moeda de liquidacao (ex: USD)") @RequestParam(required = false) String settlementCurrency,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate from,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(required = false) LocalDate to,
            @Parameter(description = "Filtro parcial pelo username do operador (visivel apenas para ADMIN)") @RequestParam(required = false) String createdBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var content = repository.findExtract(assignorName, settlementCurrency, from, to, createdBy, page, size);
        long total = repository.countExtract(assignorName, settlementCurrency, from, to, createdBy);
        return ResponseEntity.ok(PagedResult.of(content, page, size, total));
    }
}
