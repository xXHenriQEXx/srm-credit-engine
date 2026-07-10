package com.srmasset.creditengine.controller;

import com.srmasset.creditengine.dto.request.ExchangeRateRequest;
import com.srmasset.creditengine.entity.Currency;
import com.srmasset.creditengine.repository.CurrencyRepository;
import com.srmasset.creditengine.service.CurrencyExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
@Tag(name = "Currency Engine", description = "Gestao de moedas e taxas de cambio")
public class CurrencyController {

    private final CurrencyRepository currencyRepository;
    private final CurrencyExchangeService exchangeService;

    public CurrencyController(CurrencyRepository currencyRepository, CurrencyExchangeService exchangeService) {
        this.currencyRepository = currencyRepository;
        this.exchangeService = exchangeService;
    }

    @GetMapping
    @Operation(summary = "Lista as moedas cadastradas")
    public ResponseEntity<List<Currency>> list() {
        return ResponseEntity.ok(currencyRepository.findAll());
    }

    @PostMapping("/rates")
    @Operation(summary = "Atualiza (insere novo registro historico de) a taxa de cambio entre duas moedas")
    public ResponseEntity<Void> updateRate(@Valid @RequestBody ExchangeRateRequest request) {
        exchangeService.updateRate(request);
        return ResponseEntity.accepted().build();
    }
}
