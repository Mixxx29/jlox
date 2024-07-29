package org.example.lox.ast.statement;

import org.example.lox.ast.expression.Expression;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrintStatement extends Statement {
	public final Expression expression;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitPrintStatement(this);
	}

}