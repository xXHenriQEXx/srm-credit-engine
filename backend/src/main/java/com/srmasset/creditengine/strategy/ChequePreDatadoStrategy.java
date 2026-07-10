package com.srmasset.creditengine.strategy;

import com.srmasset.creditengine.entity.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ChequePreDatadoStrategy implements RiskSpreadStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.025"); // 2.5% a.m.

    @Override
    public ReceivableType getSupportedType() {
        return ReceivableType.CHEQUE_PRE_DATADO;
    }

    @Override
    public BigDecimal getMonthlySpread() {
        return MONTHLY_SPREAD;
    }
}
