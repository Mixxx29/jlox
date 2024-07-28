package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.exception.ParseErrorException;
import org.example.lox.expression.*;

import java.util.List;

@RequiredArgsConstructor
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Expression parse() {
        try {
            return parseExpression();
        } catch (ParseErrorException error) {
            return null;
        }
    }

    private Expression parseExpression() {
        return parseEquality();
    }

    private Expression parseEquality() {
        Expression expression = parseComparison();

        TokenType[] validTokenTypes = {
                TokenType.EXCLAMATION_MARK_EQUAL,
                TokenType.EQUAL_EQUAL
        };

        while (matchToken(validTokenTypes)) {
            Token operator = previousToken();
            Expression right = parseComparison();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseComparison() {
        Expression expression = parseTerm();

        TokenType[] validTokenTypes = {
                TokenType.LESS,
                TokenType.LESS_EQUAL,
                TokenType.GREATER,
                TokenType.GREATER_EQUAL
        };

        while (matchToken(validTokenTypes)) {
            Token operator = previousToken();
            Expression right = parseTerm();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseTerm() {
        Expression expression = parseFactor();

        TokenType[] validTokenTypes = {
                TokenType.PLUS,
                TokenType.MINUS
        };

        while (matchToken(validTokenTypes)) {
            Token operator = previousToken();
            Expression right = parseFactor();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseFactor() {
        Expression expression = parseUnary();

        TokenType[] validTokenTypes = {
                TokenType.ASTERISK,
                TokenType.SLASH
        };

        while (matchToken(validTokenTypes)) {
            Token operator = previousToken();
            Expression right = parseUnary();
            expression = new BinaryExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseUnary() {
        TokenType[] validTokenTypes = {
                TokenType.EXCLAMATION_MARK,
                TokenType.MINUS
        };

        if (!matchToken(validTokenTypes))
            return parsePrimary();

        Token operator = previousToken();
        Expression right = parseUnary();
        return new UnaryExpression(operator, right);
    }

    private Expression parsePrimary() {
        if (matchToken(TokenType.NUMBER))
            return new LiteralExpression(previousToken().literal, TokenType.NUMBER);

        if (matchToken(TokenType.STRING))
            return new LiteralExpression(previousToken().literal, TokenType.STRING);

        if (matchToken(TokenType.TRUE)) return new LiteralExpression(true, TokenType.TRUE);
        if (matchToken(TokenType.FALSE)) return new LiteralExpression(false, TokenType.FALSE);
        if (matchToken(TokenType.NIL)) return new LiteralExpression(null, TokenType.NIL);

        if (matchToken(TokenType.LEFT_PARENTHESIS)) {
            Expression expression = parseExpression();
            Token consumed = consumeToken(TokenType.RIGHT_PARENTHESIS, "Expected ')' after expression");
            return new GroupingExpression(expression);
        }

        throw error(peekToken(), "Expected expression");
    }

    private Token consumeToken(TokenType tokenType, String errorMessage) {
        if (checkTokenType(tokenType))
            return advanceToken();

        throw error(peekToken(), errorMessage);
    }

    private ParseErrorException error(Token token, String errorMessage) {
        Lox.error(token, errorMessage);
        return new ParseErrorException();
    }

    private boolean matchToken(TokenType... types) {
        for (TokenType type : types) {
            if (!checkTokenType(type))
                continue;

            advanceToken();
            return true;
        }
        return false;
    }

    private boolean checkTokenType(TokenType type) {
        if (isAtEnd()) return false;
        return peekToken().type == type;
    }

    private Token advanceToken() {
        if (!isAtEnd()) ++current;
        return previousToken();
    }

    private boolean isAtEnd() {
        return peekToken().type == TokenType.EOF;
    }

    private Token peekToken() {
        return tokens.get(current);
    }

    private Token previousToken() {
        return tokens.get(current - 1);
    }
}
