package org.example.lox;

import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable {
    public final String name;
    final LoxClass superClass;
    private final Map<String, LoxFunction> methods;

    public LoxClass(
            String name,
            LoxClass superClass,
            Map<String, LoxFunction> methods,
            Map<Token, LoxFunction> classMethods
    ) {
        super(null);
        this.name = name;
        this.superClass = superClass;
        this.methods = methods;

        for (Map.Entry<Token, LoxFunction> entry : classMethods.entrySet())
            set(entry.getKey(), entry.getValue());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null)
            initializer.bind(instance).call(interpreter, arguments);

        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null)
            return 0;

        return initializer.arity();
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name))
            return methods.get(name);

        if (superClass != null)
            return superClass.findMethod(name);

        return null;
    }
}
