package org.example.tool.config;

import lombok.Value;

import java.util.List;

@Value
public class ConfigData {
    String packageName;
    List<ClassConfig> classes;
}
