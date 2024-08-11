package org.example.lox;

import org.example.lox.ast.Visitor;
import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.*;

public class AstPrinter implements Visitor<String> {

    private final int INDENT_INCREMENT_LENGTH = 3;
    private final StringBuilder indent = new StringBuilder(" ".repeat(INDENT_INCREMENT_LENGTH));

    public String print(Statement statement) {
        return "──▶" + statement.accept(this);
    }

    @Override
    public String visitUnaryExpression(UnaryExpression unaryExpression) {
        StringBuilder result = new StringBuilder();

        result.append("UnaryExpression\n")
                .append(indent)
                .append("├─▶Operator ")
                .append(unaryExpression.operator.type)
                .append(" '")
                .append(unaryExpression.operator.lexeme)
                .append("'\n");

        result.append(indent);

        incrementIndent(false);
        result.append("╰─▶").append(unaryExpression.right.accept(this));
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitBinaryExpression(BinaryExpression binaryExpression) {
        StringBuilder result = new StringBuilder();

        result.append("BinaryExpression\n")
                .append(indent);

        incrementIndent(true);
        result.append("├─▶")
                .append(binaryExpression.left.accept(this));
        decrementIndent();

        result.append(indent)
                .append("├─▶Operator ")
                .append(binaryExpression.operator.type)
                .append(" '")
                .append(binaryExpression.operator.lexeme)
                .append("'\n");

        result.append(indent);

        incrementIndent(false);
        result.append("╰─▶").append(binaryExpression.right.accept(this));
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitGroupingExpression(GroupingExpression groupingExpression) {
        StringBuilder result = new StringBuilder();

        result.append("GroupingExpression\n")
                .append(indent);

        incrementIndent(false);
        result.append("╰─▶")
                .append(groupingExpression.expression.accept(this));
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitLiteralExpression(LiteralExpression literalExpression) {
        if (literalExpression.value == null) return "Literal nil\n";
        return "Literal " + literalExpression.type + " " + literalExpression.value + "\n";
    }

    @Override
    public String visitVariableExpression(VariableExpression variableExpression) {
        StringBuilder result = new StringBuilder();

        result.append("PrintStatement\n")
                .append(indent);

        incrementIndent(true);
        result.append("╰─▶Identifier ")
                .append(variableExpression.token.lexeme);
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitAssignmentExpression(AssignmentExpression assignmentExpression) {
        StringBuilder result = new StringBuilder();

        result.append("AssignmentExpression\n")
                .append(indent);

        incrementIndent(true);
        result.append("├─▶Identifier ")
                .append(assignmentExpression.token.lexeme)
                .append("\n");
        decrementIndent();

        result.append(indent);

        incrementIndent(false);
        result.append("╰─▶")
                .append(assignmentExpression.expression.accept(this));
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitLogicalExpression(LogicalExpression logicalExpression) {
        return "";
    }

    @Override
    public String visitCallExpression(CallExpression callExpression) {
        return "";
    }

    @Override
    public String visitGetExpression(GetExpression getExpression) {
        return "";
    }

    @Override
    public String visitSetExpression(SetExpression setExpression) {
        return "";
    }

    @Override
    public String visitThisExpression(ThisExpression thisExpression) {
        return "";
    }

    @Override
    public String visitSuperExpression(SuperExpression superExpression) {
        return "";
    }

    @Override
    public String visitLambdaExpression(LambdaExpression lambdaExpression) {
        return "";
    }

    @Override
    public String visitExpressionStatement(ExpressionStatement expressionStatement) {
        StringBuilder result = new StringBuilder();

        result.append("ExpressionStatement\n")
                .append(indent);

        incrementIndent(false);
        result.append("╰─▶").append(expressionStatement.expression.accept(this));
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitPrintStatement(PrintStatement printStatement) {
        StringBuilder result = new StringBuilder();

        result.append("PrintStatement\n")
                .append(indent);

        incrementIndent(false);
        result.append("╰─▶").append(printStatement.expression.accept(this));
        decrementIndent();

        return result.toString();
    }

    @Override
    public String visitVariableStatement(VariableStatement variableStatement) {
        StringBuilder result = new StringBuilder();

        result.append("VariableStatement\n")
                .append(indent);

        if (variableStatement.expression != null) {
            incrementIndent(true);
            result.append("├─▶Identifier ")
                    .append(variableStatement.token.lexeme)
                    .append("\n");
            decrementIndent();

            result.append(indent);

            incrementIndent(false);
            result.append("╰─▶").append(variableStatement.expression.accept(this));
            decrementIndent();
        } else {
            incrementIndent(true);
            result.append("╰─▶Identifier ")
                    .append(variableStatement.token.lexeme);
            decrementIndent();
        }

        return result.toString();
    }

    @Override
    public String visitBlockStatement(BlockStatement blockStatement) {
        return "";
    }

    @Override
    public String visitIfStatement(IfStatement ifStatement) {
        return "";
    }

    @Override
    public String visitWhileStatement(WhileStatement whileStatement) {
        return "";
    }

    @Override
    public String visitBreakStatement(BreakStatement breakStatement) {
        return "";
    }

    @Override
    public String visitContinueStatement(ContinueStatement continueStatement) {
        return "";
    }

    @Override
    public String visitFunctionStatement(FunctionStatement functionStatement) {
        return "";
    }

    @Override
    public String visitReturnStatement(ReturnStatement returnStatement) {
        return "";
    }

    @Override
    public String visitClassStatement(ClassStatement classStatement) {
        return "";
    }

    private void incrementIndent(boolean special) {
        int startIndex = indent.length();

        if (!special) {
            indent.append(" ".repeat(INDENT_INCREMENT_LENGTH));
            return;
        }

        for (int i = startIndex; i < startIndex + INDENT_INCREMENT_LENGTH; i++) {
            indent.append((i - INDENT_INCREMENT_LENGTH) % INDENT_INCREMENT_LENGTH == 0 ? '│' : ' ');
        }
    }

    private void decrementIndent() {
        int length = indent.length();
        indent.replace(length - INDENT_INCREMENT_LENGTH, length, "");
    }

    private void newLine(StringBuilder builder) {
        char lastChar = indent.charAt(indent.length() - 1);
        if (lastChar != '\n')
            builder.append("\n");

    }
}
