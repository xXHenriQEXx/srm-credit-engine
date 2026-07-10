package com.srmasset.creditengine.entity;

/**
 * Tipos de recebiveis suportados. Cada tipo possui uma estrategia de
 * spread de risco associada (ver package strategy).
 * Modelado como enum porque o conjunto de tipos e controlado pelo
 * negocio/compliance e novos tipos exigem uma nova estrategia de codigo
 * de qualquer forma - nao faria sentido guiar isso por uma tabela generica.
 */
public enum ReceivableType {
    DUPLICATA_MERCANTIL,
    CHEQUE_PRE_DATADO
}
