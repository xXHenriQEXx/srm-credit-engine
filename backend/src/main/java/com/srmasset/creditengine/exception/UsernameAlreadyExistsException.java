package com.srmasset.creditengine.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("Nome de usuario ja cadastrado: " + username);
    }
}
