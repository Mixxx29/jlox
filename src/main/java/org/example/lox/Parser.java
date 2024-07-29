package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.ExpressionStatement;
import org.example.lox.ast.statement.PrintStatement;
import org.example.lox.ast.statement.Statement;
import org.example.lox.ast.statement.VariableStatement;
import org.example.lox.exception.ParseErrorException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd())
            statements.add(parseDeclaration());

        return statements;
    }

    private Statement parseDeclaration() {
        try {
            if (matchToken(TokenType.VAR))
                return parseVariableDeclaration();

            return parseStatement();
        } catch (ParseErrorException error) {
            synchronize();
            return null;
        }
    }

    private Statement parseVariableDeclaration() {
        Token name = consumeToken(TokenType.IDENTIFIER, "Expected variable name");

        Expression expression = null;
        if (matchToken(TokenType.EQUAL))
            expression = parseExpression();

        consumeToken(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new VariableStatement(name, expression);
    }

    private Statement parseStatement() {
        if (matchToken(TokenType.PRINT)) return parsePrintStatement();

        return parseExpressionStatement();
    }

    private Statement parsePrintStatement() {
        Expression expression = parseExpression();
        consumeToken(TokenType.SEMICOLON, "Expected ';' after value");
        return new PrintStatement(expression);
    }

    private Statement parseExpressionStatement() {
        Expression expression = parseExpression();
        consumeToken(TokenType.SEMICOLON, "Expected ';' after expression");
        return new ExpressionStatement(expression);
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

        if (matchToken(TokenType.IDENTIFIER)) return new VariableExpression(previousToken());

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

    private void synchronize() {
        advanceToken();

        while (!isAtEnd()) {
            if (previousToken().type == TokenType.SEMICOLON) return;

            switch (peekToken().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advanceToken();
        }
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
