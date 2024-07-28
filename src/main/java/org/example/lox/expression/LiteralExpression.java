package org.example.lox.expression;

import org.example.lox.TokenType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LiteralExpression extends Expression {
	public final Object value;
	public final TokenType type;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitLiteralExpression(this);
	}

}