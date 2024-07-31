package org.example.lox.ast.expression;

import org.example.lox.Token;

import java.util.List;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CallExpression extends Expression {
	public final Expression callee;
	public final Token rightParenthesis;
	public final List<Expression> arguments;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitCallExpression(this);
	}

}