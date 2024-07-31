package org.example.lox.ast.statement;

import org.example.lox.Token;

import java.util.List;

import org.example.lox.Token;

import java.util.List;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FunctionStatement extends Statement {
	public final Token token;
	public final List<Token> parameters;
	public final List<Statement> body;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitFunctionStatement(this);
	}

}