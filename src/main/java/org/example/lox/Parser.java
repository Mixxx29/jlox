package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.*;
import org.example.lox.exception.ParseErrorException;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private int loopCount = 0;

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd())
            statements.add(parseDeclaration());

        return statements;
    }

    private Statement parseDeclaration() {
        try {
            if (matchToken(TokenType.CLASS)) return parseClass();
            if (matchToken(TokenType.FUN)) return parseFunction("function");
            if (matchToken(TokenType.VAR)) return parseVariableDeclaration();

            return parseStatement();
        } catch (ParseErrorException error) {
            synchronize();
            return null;
        }
    }

    private Statement parseClass() {
        Token name = consumeToken(TokenType.IDENTIFIER, "Expected class name");
        consumeToken(TokenType.LEFT_BRACE, "Expected '{' after class name");

        List<FunctionStatement> methods = new ArrayList<>();
        while (!checkTokenType(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            methods.add(parseFunction("method"));
        }

        consumeToken(TokenType.RIGHT_BRACE, "Expected '}' after class body");
        return new ClassStatement(name, methods);
    }

    private FunctionStatement parseFunction(String kind) {
        Token token = consumeToken(TokenType.IDENTIFIER, "Expected " + kind + " name");
        consumeToken(TokenType.LEFT_PARENTHESIS, "Expected '(' after " + kind + " name");
        List<Token> parameters = parseParameters();
        consumeToken(TokenType.RIGHT_PARENTHESIS, "Expected ')' after parameters");

        consumeToken(TokenType.LEFT_BRACE, "Expected '{' before " + kind + " body");
        List<Statement> body = parseBlock();
        return new FunctionStatement(token, parameters, body);
    }

    private List<Token> parseParameters() {
        List<Token> parameters = new ArrayList<>();
        if (!checkTokenType(TokenType.RIGHT_PARENTHESIS)) {
            do {
                if (parameters.size() >= 255)
                    error(peekToken(), "Can't have more than 255 parameters");

                parameters.add(consumeToken(TokenType.IDENTIFIER, "Expected parameter name"));
            } while (matchToken(TokenType.COMMA));
        }
        return parameters;
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
        if (matchToken(TokenType.IF)) return parseIfStatement();
        if (matchToken(TokenType.PRINT)) return parsePrintStatement();
        if (matchToken(TokenType.FOR)) return parseForStatement();
        if (matchToken(TokenType.BREAK)) return parseBreakStatement();
        if (matchToken(TokenType.RETURN)) return parseReturnStatement();
        if (matchToken(TokenType.CONTINUE)) return parseContinueStatement();
        if (matchToken(TokenType.WHILE)) return parseWhileStatement();
        if (matchToken(TokenType.LEFT_BRACE)) return new BlockStatement(parseBlock());

        return parseExpressionStatement();
    }

    private Statement parseIfStatement() {
        consumeToken(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'if'");
        Expression condition = parseExpression();
        consumeToken(TokenType.RIGHT_PARENTHESIS, "Expected ')' after condition");

        Statement thenStatement = parseStatement();
        Statement elseStatement = null;
        if (matchToken(TokenType.ELSE))
            elseStatement = parseStatement();

        return new IfStatement(condition, thenStatement, elseStatement);
    }

    private List<Statement> parseBlock() {
        List<Statement> statements = new ArrayList<>();

        while (!checkTokenType(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(parseDeclaration());
        }

        consumeToken(TokenType.RIGHT_BRACE, "Expected '}' after block");
        return statements;
    }

    private Statement parsePrintStatement() {
        Expression expression = parseExpression();
        consumeToken(TokenType.SEMICOLON, "Expected ';' after value");
        return new PrintStatement(expression);
    }

    private Statement parseForStatement() {
        consumeToken(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'for'");

        Statement initializer;
        if (matchToken(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (matchToken(TokenType.VAR)) {
            initializer = parseVariableDeclaration();
        } else {
            initializer = parseExpressionStatement();
        }

        Expression condition = null;
        if (!checkTokenType(TokenType.SEMICOLON))
            condition = parseExpression();

        consumeToken(TokenType.SEMICOLON, "Expected ';' after condition");

        Expression increment = null;
        if (!checkTokenType(TokenType.SEMICOLON))
            increment = parseExpression();

        consumeToken(TokenType.RIGHT_PARENTHESIS, " Expected ')' at the end of 'for' clause");

        incrementLoopCount();
        Statement body = parseStatement();
        decrementLoopCount();

        if (increment != null)
            body = new BlockStatement(List.of(body, new ExpressionStatement(increment)));

        if (condition == null)
            condition = new LiteralExpression(true, TokenType.TRUE);

        body = new WhileStatement(condition, body, increment);

        if (initializer != null)
            body = new BlockStatement(List.of(initializer, body));

        return body;
    }

    private Statement parseWhileStatement() {
        consumeToken(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'while'");
        Expression condition = parseExpression();
        consumeToken(TokenType.RIGHT_PARENTHESIS, "Expected ')' after condition");

        incrementLoopCount();
        Statement body = parseStatement();
        decrementLoopCount();

        return new WhileStatement(condition, body, null);
    }

    private Statement parseBreakStatement() {
        Token breakToken = previousToken();
        consumeToken(TokenType.SEMICOLON, "Expected ';' after 'break'");

        if (!isInLoop())
            throw error(breakToken, "'break' is not allowed outside of a loop");

        return new BreakStatement();
    }

    private Statement parseReturnStatement() {
        Token keyword = previousToken();
        Expression value = null;
        if (!checkTokenType(TokenType.SEMICOLON))
            value = parseExpression();

        consumeToken(TokenType.SEMICOLON, "Expected ';' after return value");
        return new ReturnStatement(keyword, value);
    }

    private Statement parseContinueStatement() {
        Token breakToken = previousToken();
        consumeToken(TokenType.SEMICOLON, "Expected ';' after 'continue'");

        if (!isInLoop())
            throw error(breakToken, "'continue' is not allowed outside of a loop");

        return new ContinueStatement();
    }

    private Statement parseExpressionStatement() {
        Expression expression = parseExpression();
        consumeToken(TokenType.SEMICOLON, "Expected ';' after expression");
        return new ExpressionStatement(expression);
    }

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseLambdaExpression() {
        consumeToken(TokenType.LEFT_PARENTHESIS, "Expected '(' after 'lambda'");
        List<Token> parameters = parseParameters();
        consumeToken(TokenType.RIGHT_PARENTHESIS, "Expected ')' after arguments");
        consumeToken(TokenType.POINTER, "Expected '->' after ')'");
        List<Statement> statements;
        consumeToken(TokenType.LEFT_BRACE, "Expected '{' after '->'");
        statements = parseBlock();
        return new LambdaExpression(parameters, statements);
    }

    private Expression parseAssignment() {
        if (matchToken(TokenType.LAMBDA))
            return parseLambdaExpression();

        Expression expression = parseOr();

        if (matchToken(TokenType.EQUAL)) {
            Token equalToken = previousToken();
            Expression value = parseAssignment();

            if (expression instanceof VariableExpression variableExpression) {
                Token token = variableExpression.token;
                return new AssignmentExpression(token, value);
            } else if (expression instanceof GetExpression getExpression) {
                return new SetExpression(getExpression.object, getExpression.name, value);
            }

            throw error(equalToken, "Invalid assignment target");
        }

        return expression;
    }

    private Expression parseOr() {
        Expression expression = parseAnd();

        while (matchToken(TokenType.OR)) {
            Token operator = previousToken();
            Expression right = parseAnd();
            expression = new LogicalExpression(expression, operator, right);
        }

        return expression;
    }

    private Expression parseAnd() {
        Expression expression = parseEquality();

        while (matchToken(TokenType.AND)) {
            Token operator = previousToken();
            Expression right = parseEquality();
            expression = new LogicalExpression(expression, operator, right);
        }

        return expression;
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
            return parseCall();

        Token operator = previousToken();
        Expression right = parseUnary();
        return new UnaryExpression(operator, right);
    }

    private Expression parseCall() {
        Expression expression = parsePrimary();

        while (true) {
            if (matchToken(TokenType.LEFT_PARENTHESIS)) {
                List<Expression> arguments = parseArguments();
                Token rightParenthesis = consumeToken(TokenType.RIGHT_PARENTHESIS, "Expected ')' after arguments");
                expression = new CallExpression(expression, rightParenthesis, arguments);
            } else if (matchToken(TokenType.DOT)) {
                Token name = consumeToken(TokenType.IDENTIFIER, "Expected property name after '.'");
                expression = new GetExpression(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private List<Expression> parseArguments() {
        List<Expression> arguments = new ArrayList<>();
        if (!checkTokenType(TokenType.RIGHT_PARENTHESIS)) {
            do {
                if (arguments.size() >= 255)
                    error(peekToken(), "Can't have more than 255 arguments");

                arguments.add(parseExpression());
            } while (matchToken(TokenType.COMMA));
        }

        return arguments;
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

    private void incrementLoopCount() {
        ++loopCount;
    }

    private void decrementLoopCount() {
        if (loopCount == 0) {
            return;
        }

        --loopCount;
    }

    private boolean isInLoop() {
        return loopCount > 0;
    }
}
