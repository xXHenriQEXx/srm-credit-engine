package com.srmasset.creditengine.controller;

import com.srmasset.creditengine.dto.request.ReceivableRequest;
import com.srmasset.creditengine.dto.response.TransactionResponse;
import com.srmasset.creditengine.entity.Transaction;
import com.srmasset.creditengine.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Cessao e liquidacao de recebiveis")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Registra e liquida um recebivel (precificacao + persistencia ACID)")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody ReceivableRequest request) {
        Transaction transaction = transactionService.createAndSettle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma transacao pelo id")
    public ResponseEntity<TransactionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(transactionService.findById(id)));
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getAssignorName(), t.getReceivableType(), t.getFaceValue(),
                t.getFaceCurrency().getCode(), t.getSettlementCurrency().getCode(), t.getDueDate(),
                t.getSettlementValue(), t.getStatus(), t.getCreatedAt()
        );
    }
}
