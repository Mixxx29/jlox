package org.example.lox.expression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupingExpression extends Expression {
	public final Expression expression;
}