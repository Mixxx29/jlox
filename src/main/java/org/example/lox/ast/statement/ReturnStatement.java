package org.example.lox.ast.statement;

import org.example.lox.Token;

import org.example.lox.ast.expression.Expression;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReturnStatement extends Statement {
	public final Token keyword;
	public final Expression value;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitReturnStatement(this);
	}

}