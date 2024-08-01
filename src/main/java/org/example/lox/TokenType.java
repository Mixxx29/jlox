package org.example.lox;

public enum TokenType {
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS,

    LEFT_BRACE,
    RIGHT_BRACE,

    DOT,
    COMMA,
    SEMICOLON,

    PLUS,
    MINUS,
    ASTERISK,
    SLASH,

    EXCLAMATION_MARK,
    EXCLAMATION_MARK_EQUAL,

    EQUAL,
    EQUAL_EQUAL,

    GREATER,
    GREATER_EQUAL,

    LESS,
    LESS_EQUAL,

    VAR,
    IDENTIFIER,
    STRING,
    NUMBER,

    TRUE,
    FALSE,

    IF,
    ELSE,
    WHILE,
    FOR,
    BREAK,
    CONTINUE,

    POINTER,

    AND,
    OR,

    CLASS,
    SUPER,
    THIS,
    NIL,

    FUN,
    RETURN,
    PRINT,
    LAMBDA,

    EOF
}
