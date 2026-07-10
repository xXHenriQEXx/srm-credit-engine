package com.srmasset.creditengine.exception;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String code) {
        super("Moeda nao cadastrada: " + code);
    }
}
