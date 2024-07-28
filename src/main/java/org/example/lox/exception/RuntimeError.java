package org.example.lox.exception;

import org.example.lox.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token operator, String message) {
        super(message);
        this.token = operator;
    }
}
