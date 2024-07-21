package org.example.lox.expression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LiteralExpression extends Expression {
	public final Object value;
}