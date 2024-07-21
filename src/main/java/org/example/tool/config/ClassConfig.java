package org.example.tool.config;

import lombok.Value;

import java.util.List;

@Value
public class ClassConfig {
    String name;
    String parentClass;
    List<FieldConfig> fields;
}
