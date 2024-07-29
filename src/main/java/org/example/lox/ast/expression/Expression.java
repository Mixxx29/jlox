package org.example.lox.ast.expression;

import org.example.lox.ast.Visitor;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Expression {

	public abstract <R> R accept(Visitor<R> visitor);
}