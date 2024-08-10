package org.example.lox;

import org.example.lox.ast.Visitor;
import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.*;
import org.example.lox.exception.Break;
import org.example.lox.exception.Continue;
import org.example.lox.exception.Return;
import org.example.lox.exception.RuntimeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Visitor<Object> {

    final Environment globals = new Environment(null);
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();
    private long startTime;

    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) (System.currentTimeMillis() - startTime);
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native_function>";
            }
        });
    }

    public void interpret(List<Statement> statements) {
        startTime = System.currentTimeMillis();
        try {
            if (statements.size() == 1 && statements.get(0) instanceof ExpressionStatement expressionStatement) {
                System.out.println(stringify(expressionStatement.expression.accept(this)));
                return;
            }

            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
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
        return lookUpVariable(variableExpression.token, variableExpression);
    }

    @Override
    public Object visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        Object value = evaluate(assignmentExpression.expression);

        Integer distance = locals.get(assignmentExpression);
        if (distance != null) {
            environment.assignAt(distance, assignmentExpression.token, value);
        } else {
            globals.assign(assignmentExpression.token, value);
        }

        return value;
    }

    @Override
    public Object visitLogicalExpression(LogicalExpression logicalExpression) {
        Object left = evaluate(logicalExpression.left);

        if (logicalExpression.operator.type == TokenType.OR) {
            if (isTrue(left)) return left;
        } else {
            if (!isTrue(left)) return left;
        }

        return evaluate(logicalExpression.right);
    }

    @Override
    public Object visitCallExpression(CallExpression callExpression) {
        Object callee = evaluate(callExpression.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expression argument : callExpression.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable function))
            throw new RuntimeError(callExpression.rightParenthesis, "Not a function");

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(
                    callExpression.rightParenthesis,
                    "Expected " + function.arity() + " arguments, but got " + arguments.size()
            );
        }

        return function.call(this, arguments);
    }

    @Override
    public Object visitLambdaExpression(LambdaExpression lambdaExpression) {
        FunctionStatement functionStatement = new FunctionStatement(
                null,
                lambdaExpression.parameters,
                lambdaExpression.body
        );

        return new LoxFunction(functionStatement, environment);
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

        environment.define(variableStatement.token.lexeme, value);
        return null;
    }

    @Override
    public Object visitBlockStatement(BlockStatement blockStatement) {
        executeBlock(blockStatement.statements, new Environment(environment));
        return null;
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement) {
        if (isTrue(evaluate(ifStatement.condition))) {
            execute(ifStatement.thenBranch);
        } else if (ifStatement.elseBranch != null) {
            execute(ifStatement.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement) {
        while (isTrue(evaluate(whileStatement.condition))) {
            try {
                execute(whileStatement.body);
            } catch (Break ignored) {
                break;
            } catch (Continue ignored) {
                if (whileStatement.increment != null) {
                    evaluate(whileStatement.increment);
                }
            }
        }

        return null;
    }

    @Override
    public Object visitBreakStatement(BreakStatement breakStatement) {
        throw new Break();
    }

    @Override
    public Object visitContinueStatement(ContinueStatement continueStatement) {
        throw new Continue();
    }

    @Override
    public Object visitFunctionStatement(FunctionStatement functionStatement) {
        LoxFunction function = new LoxFunction(functionStatement, environment);
        environment.define(functionStatement.token.lexeme, function);
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement) {
        Object value = null;
        if (returnStatement.value != null)
            value = evaluate(returnStatement.value);

        throw new Return(value);
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements)
                execute(statement);

        } finally {
            this.environment = previous;
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
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

    private Object lookUpVariable(Token token, VariableExpression variableExpression) {
        Integer distance = locals.get(variableExpression);
        if (distance != null)
            return environment.getAt(distance, token.lexeme);

        return globals.get(token);
    }

    public void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }
}
