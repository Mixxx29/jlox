package org.example;

import lombok.RequiredArgsConstructor;
import org.example.lox.Token;
import org.example.lox.exception.RuntimeError;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Enviroment {
    private final Map<String, Object> values = new HashMap<>();

    public Object get(Token token) {
        if (values.containsKey(token.lexeme))
            return values.get(token.lexeme);

        throw new RuntimeError(token, "Undefined variable '" + token.lexeme + "'");
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }
}
