package org.example.lox;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
