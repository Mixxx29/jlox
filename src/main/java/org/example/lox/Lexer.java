package org.example.lox;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PARENTHESIS);
            case ')' -> addToken(TokenType.RIGHT_PARENTHESIS);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case '.' -> addToken(TokenType.DOT);
            case ',' -> addToken(TokenType.COMMA);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.ASTERISK);
            case '/' -> scanComment();
            case '!' -> addToken(match('=') ? TokenType.EXCLAMATION_MARK_EQUAL : TokenType.EXCLAMATION_MARK );
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL );
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS );
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.EQUAL );
            case '"' -> scanString();
            case ' ', '\r', '\t' -> {} // Ignore
            case '\n' -> ++line;
            default -> handleDefault(c);
        }
    }

    private void scanComment() {
        if (match('/')) { // Is comment?
            while (peek() != '\n' && !isAtEnd()) advance(); // Consume comment characters
            return;
        }

        addToken(TokenType.SLASH);
    }

    private void scanString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') ++line;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing quote
        advance();

        // Trim the quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void handleDefault(char c) {
        if (isDigit(c))  {
            scanNumber();
            return;
        }

        if (isAlpha(c)) {
            scanIdentifier();
            return;
        }

        Lox.error(line, "Unexpected character '" + c + "'.");
    }

    private void scanNumber() {
        while (isDigit(peek())) advance();

        // Is double?
        if (peek() == '.' && isDigit(peekNext())) {
            do advance();
            while (isDigit(peek())); // Scan decimal part
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanIdentifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        ++current;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }

}
