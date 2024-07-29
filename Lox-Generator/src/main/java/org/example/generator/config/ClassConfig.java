package org.example.generator.config;

import lombok.Value;

import java.util.List;

@Value
public class ClassConfig {
    String name;
    String parentClass;
    boolean isAbstract;
    List<FieldConfig> fields;
    List<MethodConfig> methods;
}
