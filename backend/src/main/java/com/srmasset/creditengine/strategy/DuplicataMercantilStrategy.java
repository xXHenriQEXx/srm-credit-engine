package com.srmasset.creditengine.strategy;

import com.srmasset.creditengine.entity.ReceivableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DuplicataMercantilStrategy implements RiskSpreadStrategy {

    private static final BigDecimal MONTHLY_SPREAD = new BigDecimal("0.015"); // 1.5% a.m.

    @Override
    public ReceivableType getSupportedType() {
        return ReceivableType.DUPLICATA_MERCANTIL;
    }

    @Override
    public BigDecimal getMonthlySpread() {
        return MONTHLY_SPREAD;
    }
}
