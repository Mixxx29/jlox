package org.example.generator.config;

import lombok.Value;

import java.util.List;

@Value
public class ConfigData {
    String packageName;
    ClassConfig baseExpression;
    List<ClassConfig> expressions;
    ClassConfig baseStatement;
    List<ClassConfig> statements;
}
