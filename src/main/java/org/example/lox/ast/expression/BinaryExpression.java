package org.example.lox.ast.expression;

import org.example.lox.Token;

import org.example.lox.ast.Visitor;


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