package org.example.lox.ast.statement;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BreakStatement extends Statement {

	public <R> R accept(Visitor<R> visitor) {
		return visitor.visitBreakStatement(this);
	}

}