package org.example.lox.expression;

public interface Visitor<R>  {
	public R visitUnaryExpression(UnaryExpression unaryExpression);
	public R visitBinaryExpression(BinaryExpression binaryExpression);
	public R visitGroupingExpression(GroupingExpression groupingExpression);
	public R visitLiteralExpression(LiteralExpression literalExpression);
}