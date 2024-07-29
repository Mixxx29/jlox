package org.example.lox;

import org.example.Enviroment;
import org.example.lox.ast.Visitor;
import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.ExpressionStatement;
import org.example.lox.ast.statement.PrintStatement;
import org.example.lox.ast.statement.Statement;
import org.example.lox.ast.statement.VariableStatement;
import org.example.lox.exception.RuntimeError;

import java.util.List;

public class Interpreter implements Visitor<Object> {

    private Enviroment enviroment = new Enviroment();

    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression unaryExpression) {
        Object right = evaluate(unaryExpression.right);

        switch (unaryExpression.operator.type) {
            case MINUS -> {
                checkNumberOperand(unaryExpression.operator, right);
                return -(double) right;
            }

            case EXCLAMATION_MARK -> {
                return !isTrue(right);
            }

            default -> {
                return null;
            }
        }
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be number");
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression binaryExpression) {
        Object left = evaluate(binaryExpression.left);
        Object right = evaluate(binaryExpression.right);

        switch (binaryExpression.operator.type) {
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return left + (String) right;
                }

                if (left instanceof String && right instanceof Double) {
                    return left + stringify(right);
                }

                if (left instanceof Double && right instanceof String) {
                    return stringify(left) + right;
                }

                throw new RuntimeError(
                        binaryExpression.operator,
                        "Unsupported operand types"
                );
            }

            case MINUS -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                return (double) left - (double) right;
            }

            case ASTERISK -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                return (double) left * (double) right;
            }

            case SLASH -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                if ((double) right == 0)
                    throw new RuntimeError(binaryExpression.operator, "Division by zero");

                return (double) left / (double) right;
            }

            case LESS -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                return (double) left < (double) right;
            }

            case LESS_EQUAL -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                return (double) left <= (double) right;
            }

            case GREATER -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                return (double) left > (double) right;
            }

            case GREATER_EQUAL -> {
                checkNumberOperands(binaryExpression.operator, left, right);
                return (double) left >= (double) right;
            }

            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }

            case EXCLAMATION_MARK_EQUAL -> {
                return !isEqual(left, right);
            }

            default -> {
                return null;
            }
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    @Override
    public Object visitGroupingExpression(GroupingExpression groupingExpression) {
        return evaluate(groupingExpression.expression);
    }

    @Override
    public Object visitLiteralExpression(LiteralExpression literalExpression) {
        return literalExpression.value;
    }

    @Override
    public Object visitVariableExpression(VariableExpression variableExpression) {
        return enviroment.get(variableExpression.token);
    }

    @Override
    public Object visitExpressionStatement(ExpressionStatement expressionStatement) {
        evaluate(expressionStatement.expression);
        return null;
    }

    @Override
    public Object visitPrintStatement(PrintStatement printStatement) {
        Object value = evaluate(printStatement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitVariableStatement(VariableStatement variableStatement) {
        Object value = null;
        if (variableStatement.expression != null) {
            value = evaluate(variableStatement.expression);
        }

        enviroment.define(variableStatement.token.lexeme, value);
        return null;
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private boolean isTrue(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return ((Boolean) value);
        return true;
    }

    private boolean isEqual(Object value1, Object value2) {
        if (value1 == null && value2 == null) return true;
        if (value1 == null) return false;

        return value1.equals(value2);
    }

    private String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);

            return text;
        }

        return value.toString();
    }
}
