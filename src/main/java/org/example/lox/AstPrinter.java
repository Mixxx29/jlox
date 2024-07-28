package org.example.lox;

import org.example.lox.expression.*;

public class AstPrinter implements Visitor<String> {

    private final int INDENT_INCREMENT_LENGTH = 3;
    private final StringBuilder indent = new StringBuilder(" ".repeat(INDENT_INCREMENT_LENGTH));

    public String print(Expression expr) {
        return "──▶" + expr.accept(this);
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
