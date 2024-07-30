package org.example.lox.ast.expression;

import org.example.lox.Token;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssignmentExpression extends Expression {
	public final Token token;
	public final Expression expression;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitAssignmentExpression(this);
	}

}