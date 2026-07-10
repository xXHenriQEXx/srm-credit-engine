package com.srmasset.creditengine.service;

import com.srmasset.creditengine.dto.request.ExchangeRateRequest;
import com.srmasset.creditengine.entity.Currency;
import com.srmasset.creditengine.entity.ExchangeRate;
import com.srmasset.creditengine.exception.CurrencyNotFoundException;
import com.srmasset.creditengine.exception.ExchangeRateNotFoundException;
import com.srmasset.creditengine.repository.CurrencyRepository;
import com.srmasset.creditengine.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Camada de negocio (Currency Engine). Responsavel por armazenar e
 * prover as taxas de cambio usadas na conversao cross-currency.
 * Nunca sobrescrevemos uma taxa existente: cada atualizacao gera um novo
 * registro com effectiveAt = now(), preservando o historico para auditoria.
 */
@Service
public class CurrencyExchangeService {

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public CurrencyExchangeService(CurrencyRepository currencyRepository,
                                    ExchangeRateRepository exchangeRateRepository) {
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public ExchangeRate updateRate(ExchangeRateRequest request) {
        Currency base = getCurrencyOrThrow(request.baseCurrency());
        Currency quote = getCurrencyOrThrow(request.quoteCurrency());

        ExchangeRate rate = ExchangeRate.builder()
                .baseCurrency(base)
                .quoteCurrency(quote)
                .rate(request.rate())
                .effectiveAt(OffsetDateTime.now())
                .build();

        return exchangeRateRepository.save(rate);
    }

    /**
     * Retorna a taxa de conversao de "from" para "to". Se as moedas forem
     * iguais, retorna 1 (nenhuma conversao necessaria) sem consultar o banco.
     */
    @Transactional(readOnly = true)
    public BigDecimal getConversionRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }
        return exchangeRateRepository.findLatest(from, to)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new ExchangeRateNotFoundException(from, to));
    }

    private Currency getCurrencyOrThrow(String code) {
        return currencyRepository.findById(code.toUpperCase())
                .orElseThrow(() -> new CurrencyNotFoundException(code));
    }
}
