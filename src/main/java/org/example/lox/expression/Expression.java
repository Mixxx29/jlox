package org.example.lox.expression;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Expression {

	public abstract <R> R accept(Visitor<R> visitor);
}