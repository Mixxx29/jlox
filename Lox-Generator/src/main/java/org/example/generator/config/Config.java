package org.example.generator.config;

import org.example.generator.GenerateAstFiles;
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
        this.data = loadConfigData(data);
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
    private ConfigData loadConfigData(Map<String, Object> data) {
        String packageName = (String) data.get("packageName");
        if (packageName == null)
            throw new IllegalArgumentException("Config: Missing package name");


        Map<String, Object> baseExpressionClassData = (Map<String, Object>) data.get("baseExpression");
        if (baseExpressionClassData == null)
            throw new IllegalArgumentException("Config: Missing base expression class");

        ClassConfig baseExpressionClass = loadBaseExpression(baseExpressionClassData);

        List<Map<String, Object>> expressionsData = (List<Map<String, Object>>) data.get("expressions");
        List<ClassConfig> expressions = expressionsData.stream()
                .map(Config::convertClassConfig)
                .toList();

        Map<String, Object> baseStatementClassData = (Map<String, Object>) data.get("baseStatement");
        if (baseStatementClassData == null)
            throw new IllegalArgumentException("Config: Missing base statement class");

        ClassConfig baseStatementClass = loadBaseStatement(baseStatementClassData);

        List<Map<String, Object>> statementsData = (List<Map<String, Object>>) data.get("statements");
        List<ClassConfig> statements = statementsData.stream()
                .map(Config::convertClassConfig)
                .toList();

        return new ConfigData(packageName, baseExpressionClass, expressions, baseStatementClass, statements);
    }

    private ClassConfig loadBaseExpression(Map<String, Object> classData) {
        String name = (String) classData.get("name");
        if (name == null)
            throw new IllegalArgumentException("BaseExpression: Missing name");

        List<MethodConfig> methods = List.of(createAcceptMethod());

        return new ClassConfig(name, null, true, null, methods);
    }

    private ClassConfig loadBaseStatement(Map<String, Object> classData) {
        String name = (String) classData.get("name");
        if (name == null)
            throw new IllegalArgumentException("BaseStatement: Missing name");

        List<MethodConfig> methods = List.of(createAcceptMethod());

        return new ClassConfig(name, null, true, null, methods);
    }

    private MethodConfig createAcceptMethod() {
        ParameterConfig visitorParameter = new ParameterConfig("Visitor<R>", "visitor");
        List<ParameterConfig> parameters = List.of(visitorParameter);
        return new MethodConfig("accept", "<R> R", true, parameters);
    }

    @SuppressWarnings("unchecked")
    private static ClassConfig convertClassConfig(Map<String, Object> classData) {
        String name = (String) classData.get("name");
        String parentClass = (String) classData.get("parentClass");
        boolean isAbstract = (boolean) classData.getOrDefault("isAbstract", false);

        List<Map<String, Object>> fieldsData = (List<Map<String, Object>>) classData.get("fields");
        List<FieldConfig> fields = null;
        if (fieldsData != null) {
            fields = fieldsData.stream()
                    .map(Config::convertFieldConfig)
                    .toList();
        }

        List<Map<String, Object>> methodData = (List<Map<String, Object>>) classData.get("methods");
        List<MethodConfig> methods = null;
        if (methodData != null) {
            methods = methodData.stream()
                    .map(Config::convertMethodConfig)
                    .toList();
        }

        return new ClassConfig(name, parentClass, isAbstract, fields, methods);
    }

    private static FieldConfig convertFieldConfig(Map<String, Object> fieldData) {
        String name = (String) fieldData.get("name");
        String type = (String) fieldData.get("type");
        return new FieldConfig(type, name);
    }

    @SuppressWarnings("unchecked")
    private static MethodConfig convertMethodConfig(Map<String, Object> methodData) {
        String name = (String) methodData.get("name");
        String returnType = (String) methodData.get("returnType");
        boolean isAbstract = (boolean) methodData.getOrDefault("isAbstract", false);

        List<Map<String, Object>> parametersData = (List<Map<String, Object>>) methodData.get("parameters");
        List<ParameterConfig> parameters = null;
        if (parametersData != null) {
            parameters = parametersData.stream()
                    .map(Config::convertParameterConfig)
                    .toList();
        }

        return new MethodConfig(name, returnType, isAbstract, parameters);
    }

    private static ParameterConfig convertParameterConfig(Map<String, Object> parameterData) {
        String name = (String) parameterData.get("name");
        String type = (String) parameterData.get("type");
        return new ParameterConfig(type, name);
    }

    public String getPackage() {
        return data.getPackageName();
    }

    public ClassConfig getBaseExpression() {
        return data.getBaseExpression();
    }

    public List<ClassConfig> getExpressions() {
        return data.getExpressions();
    }

    public ClassConfig getBaseStatement() {
        return data.getBaseStatement();
    }

    public List<ClassConfig> getStatements() {
        return data.getStatements();
    }

    public ClassConfig getClassByName(String name) {
        if (getBaseExpression().getName().equals(name))
            return getBaseExpression();

        if (getBaseStatement().getName().equals(name))
            return getBaseStatement();

        return getExpressions().stream().filter(clazz -> clazz.getName().equals(name))
                .toList()
                .get(0);
    }
}
