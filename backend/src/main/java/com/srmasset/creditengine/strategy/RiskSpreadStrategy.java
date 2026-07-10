package com.srmasset.creditengine.strategy;

import com.srmasset.creditengine.entity.ReceivableType;

import java.math.BigDecimal;

/**
 * Estrategia de calculo de spread de risco por tipo de recebivel.
 *
 * O padrao Strategy foi escolhido para desacoplar a REGRA de risco
 * (que muda por tipo de ativo e pode evoluir independentemente, ex:
 * novas regras de compliance, tabelas de risco por rating do cedente)
 * do MOTOR de calculo do valor presente (PricingService), que e generico
 * e nao deveria conhecer detalhes de cada tipo de titulo.
 *
 * Adicionar um novo tipo de recebivel = criar uma nova classe que
 * implementa esta interface + registrar no Spring context (Strategy
 * sera injetado automaticamente na factory via List<RiskSpreadStrategy>).
 * Nenhum "if/else" ou "switch" precisa ser tocado (Open/Closed Principle).
 */
public interface RiskSpreadStrategy {

    ReceivableType getSupportedType();

    /**
     * Retorna o spread de risco mensal (ex: 0.015 para 1.5% a.m.)
     * aplicado sobre a taxa base para descontar o titulo.
     */
    BigDecimal getMonthlySpread();
}
