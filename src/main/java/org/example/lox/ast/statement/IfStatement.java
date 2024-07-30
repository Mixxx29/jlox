package org.example.lox.ast.statement;

import org.example.lox.ast.expression.Expression;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IfStatement extends Statement {
	public final Expression condition;
	public final Statement thenBranch;
	public final Statement elseBranch;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitIfStatement(this);
	}

}