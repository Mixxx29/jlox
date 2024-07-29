package org.example.lox.ast.statement;

import org.example.lox.ast.Visitor;

import org.example.lox.ast.Visitor;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Statement {

	public abstract <R> R accept(Visitor<R> visitor);
}