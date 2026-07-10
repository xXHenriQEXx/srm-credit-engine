package com.srmasset.creditengine.exception;

import java.util.UUID;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(UUID id) {
        super("Transacao nao encontrada: " + id);
    }
}
