package org.example.lox;

import lombok.AllArgsConstructor;
import org.example.lox.exception.RuntimeError;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class LoxInstance {
    private LoxClass clazz;
    private final Map<String, Object> fields = new HashMap<>();

    public Object get(Token token) {
        if (fields.containsKey(token.lexeme))
            return fields.get(token.lexeme);

        LoxFunction method = clazz.findMethod(token.lexeme);
        if (method != null) return method;

        throw new RuntimeError(token, "Undefined property '" + token.lexeme + "'");
    }

    @Override
    public String toString() {
        return clazz.name + " class instance";
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
