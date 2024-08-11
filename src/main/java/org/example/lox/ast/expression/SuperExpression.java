package org.example.lox.ast.expression;

import org.example.lox.Token;

import org.example.lox.Token;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SuperExpression extends Expression {
	public final Token keyword;
	public final Token method;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitSuperExpression(this);
	}

}