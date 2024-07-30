package org.example.lox.ast.statement;

import org.example.lox.ast.expression.Expression;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WhileStatement extends Statement {
	public final Expression condition;
	public final Statement body;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitWhileStatement(this);
	}

}