package org.example.lox.ast.expression;

import java.util.List;

import org.example.lox.Token;

import java.util.List;

import org.example.lox.ast.statement.Statement;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LambdaExpression extends Expression {
	public final List<Token> parameters;
	public final List<Statement> body;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitLambdaExpression(this);
	}

}