package org.example.lox;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    @Override
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
