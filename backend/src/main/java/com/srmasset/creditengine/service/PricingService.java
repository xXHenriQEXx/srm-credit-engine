package com.srmasset.creditengine.service;

import com.srmasset.creditengine.dto.request.ReceivableRequest;
import com.srmasset.creditengine.dto.response.PricingResultResponse;
import com.srmasset.creditengine.strategy.RiskSpreadStrategy;
import com.srmasset.creditengine.strategy.RiskSpreadStrategyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Motor de precificacao. Aplica a formula:
 *
 *   Valor Presente = Valor Face / (1 + Taxa Base + Spread) ^ Prazo
 *
 * O Spread e obtido via Strategy Pattern (RiskSpreadStrategyFactory), de
 * forma que esta classe nao conhece as regras de risco de cada tipo de
 * recebivel - apenas orquestra o calculo generico e, quando aplicavel,
 * a conversao cambial final (operacoes cross-currency).
 *
 * BigDecimal e usado em toda a cadeia de calculo (nunca double/float)
 * para evitar erros de arredondamento inaceitaveis em um motor financeiro.
 */
@Service
public class PricingService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final int MONEY_SCALE = 2;

    private final RiskSpreadStrategyFactory strategyFactory;
    private final CurrencyExchangeService exchangeService;
    private final BigDecimal baseMonthlyRate;

    public PricingService(RiskSpreadStrategyFactory strategyFactory,
                           CurrencyExchangeService exchangeService,
                           @Value("${pricing.base-monthly-rate:0.01}") BigDecimal baseMonthlyRate) {
        this.strategyFactory = strategyFactory;
        this.exchangeService = exchangeService;
        this.baseMonthlyRate = baseMonthlyRate;
    }

    public PricingResultResponse price(ReceivableRequest request) {
        RiskSpreadStrategy strategy = strategyFactory.resolve(request.receivableType());
        BigDecimal spread = strategy.getMonthlySpread();

        BigDecimal termMonths = calculateTermInMonths(request.dueDate());
        BigDecimal presentValueInFaceCurrency = calculatePresentValue(
                request.faceValue(), baseMonthlyRate, spread, termMonths);

        BigDecimal exchangeRate = exchangeService.getConversionRate(
                request.faceCurrency(), request.settlementCurrency());

        BigDecimal settlementValue = presentValueInFaceCurrency
                .multiply(exchangeRate, MC)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return new PricingResultResponse(
                request.faceValue(),
                request.faceCurrency().toUpperCase(),
                termMonths,
                baseMonthlyRate,
                spread,
                presentValueInFaceCurrency,
                exchangeRate,
                request.settlementCurrency().toUpperCase(),
                settlementValue
        );
    }

    /**
     * Valor Presente = Valor Face / (1 + Taxa Base + Spread) ^ Prazo
     * Como BigDecimal nao possui pow() com expoente fracionario nativo,
     * usamos exponenciacao via logaritmo natural: x^y = e^(y * ln(x)).
     */
    private BigDecimal calculatePresentValue(BigDecimal faceValue, BigDecimal baseRate,
                                              BigDecimal spread, BigDecimal termMonths) {
        BigDecimal discountRate = BigDecimal.ONE.add(baseRate, MC).add(spread, MC);
        BigDecimal discountFactor = pow(discountRate, termMonths);
        return faceValue.divide(discountFactor, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTermInMonths(LocalDate dueDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        if (days <= 0) {
            throw new IllegalArgumentException("data de vencimento deve ser posterior a hoje");
        }
        return BigDecimal.valueOf(days).divide(BigDecimal.valueOf(30), MC);
    }

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        double result = Math.pow(base.doubleValue(), exponent.doubleValue());
        return BigDecimal.valueOf(result);
    }
}
