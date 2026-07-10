package com.srmasset.creditengine.strategy;

import com.srmasset.creditengine.entity.ReceivableType;
import com.srmasset.creditengine.exception.UnsupportedReceivableTypeException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolve a estrategia correta em tempo de execucao a partir do
 * ReceivableType. O Spring injeta automaticamente todos os beans que
 * implementam RiskSpreadStrategy nesta lista - novas estrategias sao
 * descobertas sem alterar esta classe (Open/Closed Principle).
 */
@Component
public class RiskSpreadStrategyFactory {

    private final Map<ReceivableType, RiskSpreadStrategy> strategies;

    public RiskSpreadStrategyFactory(List<RiskSpreadStrategy> availableStrategies) {
        this.strategies = availableStrategies.stream()
                .collect(Collectors.toMap(RiskSpreadStrategy::getSupportedType, Function.identity()));
    }

    public RiskSpreadStrategy resolve(ReceivableType type) {
        RiskSpreadStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new UnsupportedReceivableTypeException(type);
        }
        return strategy;
    }
}
