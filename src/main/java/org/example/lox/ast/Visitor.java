package org.example.lox.ast;

import org.example.lox.ast.expression.*;
import org.example.lox.ast.statement.*;

public interface Visitor<R>  {
	public R visitUnaryExpression(UnaryExpression unaryExpression);
	public R visitBinaryExpression(BinaryExpression binaryExpression);
	public R visitGroupingExpression(GroupingExpression groupingExpression);
	public R visitLiteralExpression(LiteralExpression literalExpression);
	public R visitVariableExpression(VariableExpression variableExpression);
	public R visitAssignmentExpression(AssignmentExpression assignmentExpression);
	public R visitLogicalExpression(LogicalExpression logicalExpression);

	public R visitExpressionStatement(ExpressionStatement expressionStatement);
	public R visitPrintStatement(PrintStatement printStatement);
	public R visitVariableStatement(VariableStatement variableStatement);
	public R visitBlockStatement(BlockStatement blockStatement);
	public R visitIfStatement(IfStatement ifStatement);
	public R visitWhileStatement(WhileStatement whileStatement);
}