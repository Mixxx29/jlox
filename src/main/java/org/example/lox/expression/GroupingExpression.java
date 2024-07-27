package org.example.lox.expression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupingExpression extends Expression {
	public final Expression expression;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitGroupingExpression(this);
	}

}