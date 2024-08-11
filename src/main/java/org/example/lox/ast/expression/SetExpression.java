package org.example.lox.ast.expression;

import org.example.lox.Token;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetExpression extends Expression {
	public final Expression object;
	public final Token name;
	public final Expression value;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitSetExpression(this);
	}

}