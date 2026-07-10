package com.srmasset.creditengine.controller;

import com.srmasset.creditengine.dto.request.ReceivableRequest;
import com.srmasset.creditengine.dto.response.PricingResultResponse;
import com.srmasset.creditengine.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "Pricing", description = "Motor de precificacao (simulacao, sem persistencia)")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simula o calculo do valor presente de um recebivel sem gravar nada no banco")
    public ResponseEntity<PricingResultResponse> simulate(@Valid @RequestBody ReceivableRequest request) {
        return ResponseEntity.ok(pricingService.price(request));
    }
}
