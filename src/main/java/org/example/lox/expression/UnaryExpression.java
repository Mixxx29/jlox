package org.example.lox.expression;

import org.example.lox.Token;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UnaryExpression extends Expression {
	public final Token operator;
	public final Expression right;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitUnaryExpression(this);
	}

}