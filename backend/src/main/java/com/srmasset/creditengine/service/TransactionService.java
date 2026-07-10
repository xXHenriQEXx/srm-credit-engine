package com.srmasset.creditengine.service;

import com.srmasset.creditengine.dto.request.ReceivableRequest;
import com.srmasset.creditengine.dto.response.PricingResultResponse;
import com.srmasset.creditengine.entity.*;
import com.srmasset.creditengine.exception.CurrencyNotFoundException;
import com.srmasset.creditengine.exception.TransactionNotFoundException;
import com.srmasset.creditengine.repository.CurrencyRepository;
import com.srmasset.creditengine.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Camada de negocio responsavel pelo ciclo de vida da transacao de
 * cessao de credito. A criacao (precificacao + persistencia) acontece
 * dentro de uma unica transacao ACID (@Transactional): se qualquer passo
 * falhar (ex: moeda invalida, erro de banco), NADA e persistido - nao
 * existe liquidacao "pela metade".
 *
 * Concorrencia: a entidade Transaction usa @Version (optimistic locking),
 * entao duas liquidacoes concorrentes sobre o mesmo registro nunca geram
 * um estado inconsistente - a segunda falha com
 * ObjectOptimisticLockingFailureException, tratada no GlobalExceptionHandler.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CurrencyRepository currencyRepository;
    private final PricingService pricingService;

    public TransactionService(TransactionRepository transactionRepository,
                               CurrencyRepository currencyRepository,
                               PricingService pricingService) {
        this.transactionRepository = transactionRepository;
        this.currencyRepository = currencyRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public Transaction createAndSettle(ReceivableRequest request) {
        PricingResultResponse pricing = pricingService.price(request);

        Currency faceCurrency = getCurrencyOrThrow(request.faceCurrency());
        Currency settlementCurrency = getCurrencyOrThrow(request.settlementCurrency());

        Transaction transaction = Transaction.builder()
                .assignorName(request.assignorName())
                .receivableType(request.receivableType())
                .faceValue(request.faceValue())
                .faceCurrency(faceCurrency)
                .settlementCurrency(settlementCurrency)
                .dueDate(request.dueDate())
                .termMonths(pricing.termMonths())
                .spreadApplied(pricing.spreadApplied())
                .baseRateApplied(pricing.baseRateApplied())
                .exchangeRateApplied(pricing.exchangeRateApplied())
                .settlementValue(pricing.settlementValue())
                .status(TransactionStatus.SETTLED)
                .createdAt(OffsetDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    private Currency getCurrencyOrThrow(String code) {
        return currencyRepository.findById(code.toUpperCase())
                .orElseThrow(() -> new CurrencyNotFoundException(code));
    }
}
