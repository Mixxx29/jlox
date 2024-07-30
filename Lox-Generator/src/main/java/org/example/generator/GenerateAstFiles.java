package org.example.generator;

import org.example.generator.config.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GenerateAstFiles {

    private static Config config;
    private static String baseDirectoryPath;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java GenerateAstFiles <config_file> <output_directory>");
            System.exit(64);
        }

        config = new Config(args[0]);
        baseDirectoryPath = createBaseDirectory(args[1]);
        generateFiles();
    }

    private static void generateFiles() {
        // Append expressions and statements lists
        ArrayList<ClassConfig> classes = new ArrayList<>(config.getExpressions());
        classes.addAll(config.getStatements());

        // Generate Visitor interface file
        String visitorInterfaceContent = generateVisitorInterfaceContent(config.getPackage(), classes);
        createClassFile(baseDirectoryPath, "Visitor", visitorInterfaceContent);

        // Generate base expression class file
        String baseExpressionContent = generateClassContent(config.getPackage() + ".expression", config.getBaseExpression(), null);
        createClassFile(baseDirectoryPath + "\\expression", config.getBaseExpression().getName(), baseExpressionContent);

        // Generate expressions files
        for (ClassConfig classConfig : config.getExpressions()) {
            String classContent = generateClassContent(
                    config.getPackage() + ".expression",
                    classConfig,
                    config.getClassByName(classConfig.getParentClass())
            );
            createClassFile(baseDirectoryPath + "\\expression", classConfig.getName(), classContent);
        }

        // Generate base statement class file
        String baseStatementContent = generateClassContent(config.getPackage() + ".statement", config.getBaseStatement(), null);
        createClassFile(baseDirectoryPath + "\\statement", config.getBaseStatement().getName(), baseStatementContent);

        // Generate statements files
        for (ClassConfig classConfig : config.getStatements()) {
            String classContent = generateClassContent(
                    config.getPackage() + ".statement",
                    classConfig,
                    config.getClassByName(classConfig.getParentClass())
            );
            createClassFile(baseDirectoryPath + "\\statement", classConfig.getName(), classContent);
        }
    }

    private static String createBaseDirectory(String baseDirectoryPath) {
        baseDirectoryPath += "\\\\" + config.getPackage().replace('.', '\\');
        Path baseDirectory = Paths.get(baseDirectoryPath);

        if (Files.exists(baseDirectory))
            deleteDirectory(baseDirectory);

        Path expressionDirectory = Paths.get(baseDirectoryPath + "\\expression");
        if (Files.exists(expressionDirectory))
            deleteDirectory(expressionDirectory);

        Path statementsDirectory = Paths.get(baseDirectoryPath + "\\statement");
        if (Files.exists(statementsDirectory))
            deleteDirectory(statementsDirectory);

        try {
            Files.createDirectories(baseDirectory);
            Files.createDirectories(expressionDirectory);
            Files.createDirectories(statementsDirectory);
            return baseDirectory.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteDirectory(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + p, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateVisitorInterfaceContent(String packageName, List<ClassConfig> classes) {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n\n");

        builder.append("import ").append(packageName).append(".expression.*;\n");
        builder.append("import ").append(packageName).append(".statement.*;\n\n");

        builder.append("public interface Visitor<R>  {\n");

        boolean separated = false;
        for (ClassConfig classConfig : classes) {
            if (classConfig.isAbstract())
                continue;

            if (!separated && isStatementClass(classConfig)) {
                builder.append("\n");
                separated = true;
            }

            builder.append("\t")
                    .append("public R visit")
                    .append(classConfig.getName())
                    .append("(")
                    .append(classConfig.getName())
                    .append(" ").append(classConfig.getName().toLowerCase().charAt(0))
                    .append(classConfig.getName().substring(1))
                    .append(");\n");
        }

        builder.append("}");

        return builder.toString();
    }

    private static String generateClassContent(String packageName, ClassConfig classConfig, ClassConfig parentClass) {
        StringBuilder builder = new StringBuilder();

        builder.append("package ").append(packageName).append(";\n");

        addImports(packageName, builder, classConfig);

        builder.append("\nimport ").append(config.getPackage()).append(".Visitor;\n\n");
        builder.append("\nimport lombok.RequiredArgsConstructor;\n\n");
        builder.append("@RequiredArgsConstructor\n");
        builder.append("public ");

        if (classConfig.isAbstract())
            builder.append("abstract ");

        builder.append("class ");

        builder.append(classConfig.getName());

        if (classConfig.getParentClass() != null)
            builder.append(" extends ").append(classConfig.getParentClass());

        builder.append(" {\n");

        if (classConfig.getFields() != null) {
            for (FieldConfig fieldConfig : classConfig.getFields()) {
                builder.append("\tpublic final ")
                        .append(fieldConfig.getType())
                        .append(" ")
                        .append(fieldConfig.getName())
                        .append(";\n");
            }
        }

        String methodsContent = generateMethodsContent(classConfig);
        if (!methodsContent.isEmpty())
            builder.append("\n");

        builder.append(methodsContent);

        // Generate base class files
        if (parentClass != null) {
            String overrideMethods = generateParentMethodsContent(classConfig, parentClass);
            if (!overrideMethods.isEmpty())
                builder.append("\n");

            builder.append(overrideMethods);
        }

        builder.append("}");

        return builder.toString();
    }

    private static String generateMethodsContent(ClassConfig classConfig) {
        StringBuilder builder = new StringBuilder();
        if (classConfig.getMethods() == null)
            return "";

        for (MethodConfig methodConfig : classConfig.getMethods()) {
            builder.append("\tpublic ");
            if (methodConfig.isAbstract())
                builder.append("abstract ");

            builder.append(methodConfig.getReturnType())
                    .append(" ")
                    .append(methodConfig.getName())
                    .append("(");

            if (methodConfig.getParameters() != null) {
                for (ParameterConfig parameterConfig : methodConfig.getParameters()) {
                    builder.append(parameterConfig.getType())
                            .append(" ")
                            .append(parameterConfig.getName())
                            .append(", ");
                }

                // Remove last ', '
                builder.delete(builder.length() - 2, builder.length());
            }

            builder.append(")");

            if (!methodConfig.isAbstract()) {
                builder.append(" {\n");
                if (methodConfig.getName().equals("accept")) {
                    builder.append("\t\treturn visitor.visit")
                            .append(classConfig.getName())
                            .append("(self);\n");
                } else {
                    builder.append("\tthrow new RuntimeException(\"")
                            .append(classConfig.getName())
                            .append(": Method '")
                            .append(methodConfig.getName())
                            .append("' not implemented\");");
                }
                builder.append("\t}\n\n");
            } else {
                builder.append(";\n");
            }
        }

        return builder.toString();
    }

    private static String generateParentMethodsContent(ClassConfig classConfig, ClassConfig parentClass) {
        StringBuilder builder = new StringBuilder();
        if (parentClass.getMethods() == null)
            return "";

        for (MethodConfig methodConfig : parentClass.getMethods()) {
            builder.append("\tpublic ");

            builder.append(methodConfig.getReturnType())
                    .append(" ")
                    .append(methodConfig.getName())
                    .append("(");

            if (methodConfig.getParameters() != null) {
                for (ParameterConfig parameterConfig : methodConfig.getParameters()) {
                    builder.append(parameterConfig.getType())
                            .append(" ")
                            .append(parameterConfig.getName())
                            .append(", ");
                }

                // Remove last ', '
                builder.delete(builder.length() - 2, builder.length());
            }

            builder.append(") {\n");

            if (methodConfig.getName().equals("accept")) {
                builder.append("\t\treturn visitor.visit")
                        .append(classConfig.getName())
                        .append("(this);\n");
            } else {
                builder.append("\tthrow new RuntimeException(\"")
                        .append(classConfig.getName())
                        .append(": Method '")
                        .append(methodConfig.getName())
                        .append("' not implemented\");");
            }
            builder.append("\t}\n\n");
        }

        return builder.toString();
    }

    private static void addImports(String packageName, StringBuilder builder, ClassConfig classConfig) {
        if (classConfig.getFields() != null) {
            for (FieldConfig fieldConfig : classConfig.getFields()) {
                String className = fieldConfig.getType();
                String namePart = className.split("<")[0];
                
                String typePackage = getPackageName(namePart);
                if (typePackage != null && !typePackage.equals(packageName)) {
                    insertImport(builder, typePackage + "." + namePart);
                }

                if (!className.contains("<") || !className.contains(">"))
                    continue;

                String paramsPart = className.substring(className.indexOf('<') + 1, className.indexOf('>'));
                String[] classNames = paramsPart.split(",\\s*");
                for (String name : classNames) {
                    typePackage = getPackageName(name);
                    if (typePackage != null && !typePackage.equals(packageName)) {
                        insertImport(builder, typePackage + "." + name);
                    }
                }
            }
        }

        if (classConfig.getMethods() != null) {
            for (MethodConfig methodConfig : classConfig.getMethods()) {
                for (ParameterConfig parameterConfig : methodConfig.getParameters()) {
                    String typePackage = getPackageName(parameterConfig.getType());
                    if (typePackage != null && !typePackage.equals(packageName)) {
                        insertImport(builder, typePackage + "." + parameterConfig.getType());
                    }
                }
            }
        }
    }

    private static void insertImport(StringBuilder builder, String importValue) {
        builder.append("\nimport ")
                .append(importValue)
                .append(";\n");
    }

    private static String getPackageName(String className) {
        if (className.equals("List")) return "java.util";

        File baseDirectory = Paths.get(baseDirectoryPath.split("java")[0] + "java").toFile();
        Optional<String> packageName = findPackage(baseDirectory, className, baseDirectory.toPath());
        return packageName.orElse(null);
    }

    private static Optional<String> findPackage(File directory, String className, Path sourcePath) {
        File[] files = directory.listFiles();
        if (files == null) return Optional.empty();

        for (File file : files) {
            if (file.isDirectory()) {
                Optional<String> result = findPackage(file, className, sourcePath);
                if (result.isPresent()) return result;
            } else if (file.isFile() && file.getName().equals(className + ".java")) {
                String relativePath = sourcePath.relativize(file.toPath()).toString();
                String packageName = relativePath.replace(File.separatorChar, '.');

                // Remove the .className and .java
                packageName = packageName.substring(0, packageName.length() - (className.length() + 6));
                return Optional.of(packageName);
            }
        }

        return Optional.empty();
    }

    private static void createClassFile(String directory, String className, String classContent) {
        Path filepath = Paths.get(directory).resolve(className + ".java");
        try {
            Files.writeString(filepath, classContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createDirectory(String directoryPath) {
        Path filepath = Paths.get(directoryPath);
        try {
            Files.createDirectory(filepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isExpressionClass(ClassConfig classConfig) {
        return classConfig.getName().endsWith("Expression");
    }

    private static boolean isStatementClass(ClassConfig classConfig) {
        return classConfig.getName().endsWith("Statement");
    }
}
