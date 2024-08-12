package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.ast.Visitor;
import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@RequiredArgsConstructor
public class Resolver implements Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    @Override
    public Void visitUnaryExpression(UnaryExpression unaryExpression) {
        resolve(unaryExpression.right);
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression binaryExpression) {
        resolve(binaryExpression.left);
        resolve(binaryExpression.right);
        return null;
    }

    @Override
    public Void visitGroupingExpression(GroupingExpression groupingExpression) {
        resolve(groupingExpression.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(LiteralExpression literalExpression) {
        return null;
    }

    @Override
    public Void visitVariableExpression(VariableExpression variableExpression) {
        if (!scopes.empty() && scopes.peek().get(variableExpression.token.lexeme) == Boolean.FALSE)
            Lox.error(variableExpression.token, "Undefined variable");

        resolveLocal(variableExpression, variableExpression.token);
        return null;
    }

    @Override
    public Void visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        resolve(assignmentExpression.expression);
        resolveLocal(assignmentExpression, assignmentExpression.token);
        return null;
    }

    @Override
    public Void visitLogicalExpression(LogicalExpression logicalExpression) {
        resolve(logicalExpression.left);
        resolve(logicalExpression.right);
        return null;
    }

    @Override
    public Void visitCallExpression(CallExpression callExpression) {
        resolve(callExpression.callee);

        for (Expression argument : callExpression.arguments)
            resolve(argument);

        return null;
    }

    @Override
    public Void visitGetExpression(GetExpression getExpression) {
        resolve(getExpression.object);
        return null;
    }

    @Override
    public Void visitSetExpression(SetExpression setExpression) {
        resolve(setExpression.value);
        resolve(setExpression.object);
        return null;
    }

    @Override
    public Void visitThisExpression(ThisExpression thisExpression) {
        if (currentClass == ClassType.NONE) {
            Lox.error(thisExpression.keyword, "Can't use 'this' outside of a class");
            return null;
        }

        if (currentFunction == FunctionType.CLASS_METHOD) {
            Lox.error(thisExpression.keyword, "Can't use 'this' in static methods");
            return null;
        }

        resolveLocal(thisExpression, thisExpression.keyword);
        return null;
    }

    @Override
    public Void visitSuperExpression(SuperExpression superExpression) {
        if (currentClass == ClassType.NONE) {
            Lox.error(superExpression.keyword, "Can't use 'super' outside of a class");
        } else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(superExpression.keyword, "Can't use 'super' in class without superclass");
        }
        resolveLocal(superExpression, superExpression.keyword);
        return null;
    }

    @Override
    public Void visitLambdaExpression(LambdaExpression lambdaExpression) {
        resolveLambda(lambdaExpression, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatement expressionStatement) {
        resolve(expressionStatement.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(PrintStatement printStatement) {
        resolve(printStatement.expression);
        return null;
    }

    @Override
    public Void visitVariableStatement(VariableStatement variableStatement) {
        declare(variableStatement.token);
        if (variableStatement.expression != null)
            resolve(variableStatement.expression);

        define(variableStatement.token);
        return null;
    }

    @Override
    public Void visitBlockStatement(BlockStatement blockStatement) {
        beginScope();
        resolve(blockStatement.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStatement(IfStatement ifStatement) {
        resolve(ifStatement.condition);
        resolve(ifStatement.thenBranch);
        if (ifStatement.elseBranch != null)
            resolve(ifStatement.elseBranch);

        return null;
    }

    @Override
    public Void visitWhileStatement(WhileStatement whileStatement) {
        resolve(whileStatement.condition);
        resolve(whileStatement.body);
        return null;
    }

    @Override
    public Void visitBreakStatement(BreakStatement breakStatement) {
        return null;
    }

    @Override
    public Void visitContinueStatement(ContinueStatement continueStatement) {
        return null;
    }

    @Override
    public Void visitFunctionStatement(FunctionStatement functionStatement) {
        declare(functionStatement.token);
        define(functionStatement.token);
        resolveFunction(functionStatement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitReturnStatement(ReturnStatement returnStatement) {
        if (currentFunction == FunctionType.NONE)
            Lox.error(returnStatement.keyword, "Can't use return statement outside of a function");

        if (returnStatement.value != null) {
            if (currentFunction == FunctionType.INITIALIZER)
                Lox.error(returnStatement.keyword, "Can't return a value from an initializer");

            resolve(returnStatement.value);
        }

        return null;
    }

    @Override
    public Void visitClassStatement(ClassStatement classStatement) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(classStatement.name);
        define(classStatement.name);

        if (classStatement.superclass != null) {
            if (classStatement.superclass.token.lexeme.equals(classStatement.name.lexeme))
                Lox.error(classStatement.superclass.token, "A class can't inherit it self");

            currentClass = ClassType.SUBCLASS;

            resolve(classStatement.superclass);

            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (FunctionStatement method : classStatement.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.token.lexeme.equals("init"))
                declaration = FunctionType.INITIALIZER;

            resolveFunction(method, declaration);
        }

        for (FunctionStatement method : classStatement.classMethods)
            resolveFunction(method, FunctionType.CLASS_METHOD);

        endScope();

        if (classStatement.superclass != null)
            endScope();

        currentClass = enclosingClass;

        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    public void resolve(List<Statement> statements) {
        for (Statement statement : statements)
            resolve(statement);
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void resolveLocal(Expression expression, Token token) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(token.lexeme)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(FunctionStatement functionStatement, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token parameter : functionStatement.parameters) {
            declare(parameter);
            define(parameter);
        }

        resolve(functionStatement.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    private void resolveLambda(LambdaExpression lambdaExpression, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token parameter : lambdaExpression.parameters) {
            declare(parameter);
            define(parameter);
        }

        resolve(lambdaExpression.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    private void declare(Token token) {
        if (scopes.empty())
            return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(token.lexeme))
            Lox.error(token, "Variable already declared in this scope");

        scope.put(token.lexeme, false);
    }

    private void define(Token token) {
        if (scopes.empty())
            return;

        Map<String, Boolean> scope = scopes.peek();
        scope.put(token.lexeme, true);
    }
}
