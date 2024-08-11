package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.ast.statement.FunctionStatement;
import org.example.lox.exception.Return;

import java.util.List;

@RequiredArgsConstructor
public class LoxFunction implements LoxCallable {
    private final FunctionStatement declaration;
    private final Environment closure;
    private final boolean isInitializer;

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer)
            return closure.getAt(0, "this");

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

    public LoxFunction bind(LoxInstance loxInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(declaration, environment, isInitializer);
    }
}
