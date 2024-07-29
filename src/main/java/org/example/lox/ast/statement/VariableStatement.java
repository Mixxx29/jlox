package org.example.lox.ast.statement;

import org.example.lox.Token;

import org.example.lox.ast.expression.Expression;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VariableStatement extends Statement {
	public final Token token;
	public final Expression expression;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitVariableStatement(this);
	}

}