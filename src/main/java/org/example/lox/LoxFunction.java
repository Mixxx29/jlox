package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.ast.statement.FunctionStatement;

import java.util.List;

@RequiredArgsConstructor
public class LoxFunction implements LoxCallable {
    private final FunctionStatement declaration;

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        interpreter.executeBlock(declaration.body, environment);
        return null;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public String toString() {
        return "<function " + declaration.token.lexeme + ">";
    }
}
