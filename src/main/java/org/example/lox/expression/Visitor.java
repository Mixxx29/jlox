package org.example.lox.expression;

public interface Visitor<R>  {
	public R visitUnaryExpression(UnaryExpression unaryexpression);
	public R visitBinaryExpression(BinaryExpression binaryexpression);
	public R visitGroupingExpression(GroupingExpression groupingexpression);
	public R visitLiteralExpression(LiteralExpression literalexpression);
}