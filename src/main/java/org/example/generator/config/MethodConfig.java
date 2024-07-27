package org.example.generator.config;

import lombok.Value;

import java.util.List;

@Value
public class MethodConfig {
    String name;
    String returnType;
    boolean isAbstract;
    List<ParameterConfig> parameters;
}
