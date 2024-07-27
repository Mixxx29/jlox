package org.example.lox.expression;

import org.example.lox.Token;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BinaryExpression extends Expression {
	public final Expression left;
	public final Token operator;
	public final Expression right;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitBinaryExpression(this);
	}

}