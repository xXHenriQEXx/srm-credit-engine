package com.srmasset.creditengine.strategy;

import com.srmasset.creditengine.entity.ReceivableType;
import com.srmasset.creditengine.exception.UnsupportedReceivableTypeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskSpreadStrategyFactoryTest {

    private final RiskSpreadStrategyFactory factory = new RiskSpreadStrategyFactory(
            List.of(new DuplicataMercantilStrategy(), new ChequePreDatadoStrategy()));

    @Test
    void deveResolverEstrategiaParaDuplicataMercantil() {
        RiskSpreadStrategy strategy = factory.resolve(ReceivableType.DUPLICATA_MERCANTIL);

        assertThat(strategy).isInstanceOf(DuplicataMercantilStrategy.class);
        assertThat(strategy.getMonthlySpread()).isEqualByComparingTo(new BigDecimal("0.015"));
    }

    @Test
    void deveResolverEstrategiaParaChequePreDatado() {
        RiskSpreadStrategy strategy = factory.resolve(ReceivableType.CHEQUE_PRE_DATADO);

        assertThat(strategy).isInstanceOf(ChequePreDatadoStrategy.class);
        assertThat(strategy.getMonthlySpread()).isEqualByComparingTo(new BigDecimal("0.025"));
    }

    @Test
    void deveLancarExcecaoQuandoTipoNaoTemEstrategiaRegistrada() {
        RiskSpreadStrategyFactory emptyFactory = new RiskSpreadStrategyFactory(List.of());

        assertThatThrownBy(() -> emptyFactory.resolve(ReceivableType.DUPLICATA_MERCANTIL))
                .isInstanceOf(UnsupportedReceivableTypeException.class)
                .hasMessageContaining("DUPLICATA_MERCANTIL");
    }
}
