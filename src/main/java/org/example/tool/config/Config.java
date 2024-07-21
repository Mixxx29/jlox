package org.example.tool.config;

import org.example.tool.GenerateAstFiles;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Config {
    private final ConfigData data;

    public Config(String filename) {
        if (!filename.endsWith(".yaml") && !filename.endsWith(".yml"))
            throw new IllegalArgumentException("Invalid config file: " + filename);

        Map<String, Object> data = loadYaml(filename);
        this.data = convert(data);
    }

    private Map<String, Object> loadYaml(String filename) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = GenerateAstFiles.class.getClassLoader().getResourceAsStream(filename)) {
            return yaml.load(inputStream);
        } catch (IOException ignored) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
    }

    @SuppressWarnings("unchecked")
    private ConfigData convert(Map<String, Object> data) {
        String packageName = (String) data.get("packageName");
        List<Map<String, Object>> classesData = (List<Map<String, Object>>) data.get("classes");
        List<ClassConfig> classes = classesData.stream()
                .map(Config::convertClassConfig)
                .toList();

        return new ConfigData(packageName, classes);
    }

    @SuppressWarnings("unchecked")
    private static ClassConfig convertClassConfig(Map<String, Object> classData) {
        String name = (String) classData.get("name");
        String parentClass = (String) classData.get("parentClass");
        List<Map<String, Object>> fieldsData = (List<Map<String, Object>>) classData.get("fields");
        List<FieldConfig> fields = null;
        if (fieldsData != null) {
            fields = fieldsData.stream()
                    .map(Config::convertFieldConfig)
                    .toList();
        }

        return new ClassConfig(name, parentClass, fields);
    }

    private static FieldConfig convertFieldConfig(Map<String, Object> fieldData) {
        String name = (String) fieldData.get("name");
        String type = (String) fieldData.get("type");
        return new FieldConfig(type, name);
    }

    public String getPackage() {
        return data.getPackageName();
    }

    public List<ClassConfig> getClasses() {
        return data.getClasses();
    }
}
