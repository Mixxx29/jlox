package org.example.generator;

import org.example.generator.config.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GenerateAstFiles {
    public static void main(String[] args) {
        if (args.length != 2 || (!args[0].equals("-e") && !args[0].equals("-s"))) {
            System.err.println("Usage: java GenerateAstFiles [ -e | -s ] <config_file>");
            System.exit(64);
        }

        Config config = new Config(args[1]);
        generateFiles(config);
    }

    private static void generateFiles(Config config) {
        String baseDirectory = createBaseDirectory(config);

        // Generate Visitor interface file
        String visitorInterfaceContent = generateVisitorInterfaceContent(config.getPackage(), config.getClasses());
        createClassFile(baseDirectory, "Visitor", visitorInterfaceContent);

        // Generate Base class file
        String baseClassContent = generateClassContent(config.getPackage(), config.getBaseClass(), null);
        createClassFile(baseDirectory, config.getBaseClass().getName(), baseClassContent);

        // Generate classes files
        for (ClassConfig classConfig : config.getClasses()) {
            String classContent = generateClassContent(
                    config.getPackage(),
                    classConfig,
                    config.getClassByName(classConfig.getParentClass())
            );
            createClassFile(baseDirectory, classConfig.getName(), classContent);
        }
    }

    private static String createBaseDirectory(Config config) {
        String baseDirectory = "src/main/java/" + config.getPackage().replace('.', '/');
        Path directory = Paths.get(baseDirectory);
        if (Files.exists(directory))
            deleteDirectory(directory);

        try {
            Files.createDirectories(directory);
            return baseDirectory;
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

        builder.append("public interface Visitor<R>  {\n");

        for (ClassConfig classConfig : classes) {
            if (classConfig.isAbstract())
                continue;

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
                String typePackage = getPackageName(fieldConfig.getType());
                if (typePackage != null && !typePackage.equals(packageName)) {
                    builder.append("\nimport ")
                            .append(typePackage)
                            .append(".")
                            .append(fieldConfig.getType())
                            .append(";\n");
                }
            }
        }
    }

    private static String getPackageName(String className) {
        File baseDirectory = Paths.get("")
                .toAbsolutePath()
                .resolve("src")
                .resolve("main")
                .resolve("java")
                .toFile();

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
}
