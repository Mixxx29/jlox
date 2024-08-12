package org.example.lox;

import lombok.RequiredArgsConstructor;
import org.example.lox.exception.RuntimeError;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Environment {
    public final Environment parent;
    private final Map<String, Object> values = new HashMap<>();

    public Object get(Token token) {
        if (values.containsKey(token.lexeme))
            return values.get(token.lexeme);

        if (parent != null)
            return parent.get(token);

        throw new RuntimeError(token, "Undefined variable '" + token.lexeme + "'");
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.parent;
        }

        return environment;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public void assign(Token token, Object value) {
        if (values.containsKey(token.lexeme)) {
            values.put(token.lexeme, value);
            return;
        }

        if (parent != null) {
            parent.assign(token, value);
            return;
        }

        throw new RuntimeError(token, "Undefined variable '" + token.lexeme + "'");
    }

    public void assignAt(int distance, Token token, Object value) {
        ancestor(distance).values.put(token.lexeme, value);
    }
}
