package com.srmasset.creditengine.service;

import com.srmasset.creditengine.dto.request.ReceivableRequest;
import com.srmasset.creditengine.dto.response.PricingResultResponse;
import com.srmasset.creditengine.entity.ReceivableType;
import com.srmasset.creditengine.strategy.ChequePreDatadoStrategy;
import com.srmasset.creditengine.strategy.DuplicataMercantilStrategy;
import com.srmasset.creditengine.strategy.RiskSpreadStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testes do motor de precificacao. Foco em validar:
 * - a formula de valor presente (deve descontar o valor face)
 * - que o spread correto e aplicado por tipo de recebivel (via Strategy real, sem mock)
 * - a conversao cambial em operacoes cross-currency
 * - a rejeicao de vencimentos no passado
 */
@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private CurrencyExchangeService exchangeService;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        RiskSpreadStrategyFactory factory = new RiskSpreadStrategyFactory(
                List.of(new DuplicataMercantilStrategy(), new ChequePreDatadoStrategy()));
        pricingService = new PricingService(factory, exchangeService, new BigDecimal("0.01"));
    }

    @Test
    void deveDescontarValorFaceAplicandoTaxaBaseESpread() {
        when(exchangeService.getConversionRate("BRL", "BRL")).thenReturn(BigDecimal.ONE);

        ReceivableRequest request = new ReceivableRequest(
                "Empresa Cedente Ltda", ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("10000.00"), "BRL", "BRL", LocalDate.now().plusDays(30));

        PricingResultResponse result = pricingService.price(request);

        // Com taxa base 1% + spread 1.5% ao mes, para ~1 mes o valor presente deve ser
        // sensivelmente menor que o valor de face, mas nunca zero ou negativo.
        assertThat(result.presentValueInFaceCurrency())
                .isLessThan(request.faceValue())
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(result.spreadApplied()).isEqualByComparingTo(new BigDecimal("0.015"));
    }

    @Test
    void chequePreDatadoDeveTerDeagioMaiorQueDuplicataParaMesmoPrazo() {
        when(exchangeService.getConversionRate("BRL", "BRL")).thenReturn(BigDecimal.ONE);
        LocalDate dueDate = LocalDate.now().plusDays(60);

        PricingResultResponse duplicata = pricingService.price(new ReceivableRequest(
                "Cedente A", ReceivableType.DUPLICATA_MERCANTIL, new BigDecimal("10000.00"),
                "BRL", "BRL", dueDate));

        PricingResultResponse cheque = pricingService.price(new ReceivableRequest(
                "Cedente A", ReceivableType.CHEQUE_PRE_DATADO, new BigDecimal("10000.00"),
                "BRL", "BRL", dueDate));

        // Maior spread de risco (cheque) implica maior deságio -> valor presente menor.
        assertThat(cheque.presentValueInFaceCurrency())
                .isLessThan(duplicata.presentValueInFaceCurrency());
    }

    @Test
    void deveAplicarTaxaDeCambioEmOperacaoCrossCurrency() {
        when(exchangeService.getConversionRate("BRL", "USD")).thenReturn(new BigDecimal("0.20"));

        ReceivableRequest request = new ReceivableRequest(
                "Cedente Exportador", ReceivableType.DUPLICATA_MERCANTIL,
                new BigDecimal("10000.00"), "BRL", "USD", LocalDate.now().plusDays(30));

        PricingResultResponse result = pricingService.price(request);

        BigDecimal expected = result.presentValueInFaceCurrency()
                .multiply(new BigDecimal("0.20"))
                .setScale(2, java.math.RoundingMode.HALF_UP);

        assertThat(result.settlementValue()).isEqualByComparingTo(expected);
        assertThat(result.settlementCurrency()).isEqualTo("USD");
    }

    @Test
    void deveRejeitarDataDeVencimentoNoPassado() {
        ReceivableRequest request = new ReceivableRequest(
                "Cedente A", ReceivableType.DUPLICATA_MERCANTIL, new BigDecimal("1000.00"),
                "BRL", "BRL", LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> pricingService.price(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
