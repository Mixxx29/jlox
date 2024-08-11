package org.example.lox.ast.expression;

import org.example.lox.Token;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThisExpression extends Expression {
	public final Token keyword;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitThisExpression(this);
	}

}