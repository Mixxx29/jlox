package org.example.generator.config;

import lombok.Value;

import java.util.List;

@Value
public class InterfaceConfig {
    String name;
    List<MethodConfig> methods;
}
