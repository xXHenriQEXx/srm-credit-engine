package com.srmasset.creditengine.exception;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(String base, String quote) {
        super(String.format("Taxa de cambio nao encontrada para o par %s/%s", base, quote));
    }
}
