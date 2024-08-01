package org.example.lox.exception;

public class Continue extends RuntimeException {
    public Continue() {
        super(null, null, false, false);
    }
}
