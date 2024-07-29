package org.example.lox.ast.expression;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupingExpression extends Expression {
	public final Expression expression;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitGroupingExpression(this);
	}

}