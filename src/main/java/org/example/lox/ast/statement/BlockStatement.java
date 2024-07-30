package org.example.lox.ast.statement;

import java.util.List;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BlockStatement extends Statement {
	public final List<Statement> statements;

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitBlockStatement(this);
	}

}