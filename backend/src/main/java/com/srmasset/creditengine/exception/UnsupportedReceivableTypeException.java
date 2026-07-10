package com.srmasset.creditengine.exception;

import com.srmasset.creditengine.entity.ReceivableType;

public class UnsupportedReceivableTypeException extends RuntimeException {
    public UnsupportedReceivableTypeException(ReceivableType type) {
        super("Tipo de recebivel nao suportado: " + type);
    }
}
