package org.example.lox.ast.statement;

import org.example.lox.Token;

import org.example.lox.ast.expression.VariableExpression;

import java.util.List;

import java.util.List;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassStatement extends Statement {
	public final Token name;
	public final VariableExpression superclass;
	public final List<FunctionStatement> methods;
	public final List<FunctionStatement> classMethods;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitClassStatement(this);
	}

}