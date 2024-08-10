package org.example.lox.ast.expression;

import org.example.lox.Token;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetExpression extends Expression {
	public final Expression object;
	public final Token name;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitGetExpression(this);
	}

}