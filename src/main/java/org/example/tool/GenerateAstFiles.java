package org.example.tool;

import org.example.tool.config.ClassConfig;
import org.example.tool.config.Config;
import org.example.tool.config.FieldConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
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
        for (ClassConfig classConfig : config.getClasses()) {
            String classContent = generateClassContent(config.getPackage(), classConfig);
            createClassFile(baseDirectory, classConfig.getName(), classContent);
        }
    }

    private static String createBaseDirectory(Config config) {
        String baseDirectory = "src/main/java/" + config.getPackage().replace('.', '/');
        if (Files.exists(Paths.get(baseDirectory)))
            deleteDirectory(Paths.get(baseDirectory));
        
        try {
            Files.createDirectories(Paths.get(baseDirectory));
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

    private static String generateClassContent(String packageName, ClassConfig classConfig) {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(packageName).append(";\n");

        // Add other class imports
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

        builder.append("\nimport lombok.RequiredArgsConstructor;\n\n");
        builder.append("@RequiredArgsConstructor\n");
        builder.append("public class ").append(classConfig.getName());

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

        builder.append("}");

        return builder.toString();
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
