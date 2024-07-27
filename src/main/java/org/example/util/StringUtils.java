package org.example.util;

public class StringUtils {
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder camelCaseString = new StringBuilder();
        boolean nextCharUpperCase = false;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (Character.isWhitespace(currentChar) || currentChar == '_' || currentChar == '-') {
                nextCharUpperCase = true;
            } else if (nextCharUpperCase) {
                camelCaseString.append(Character.toUpperCase(currentChar));
                nextCharUpperCase = false;
            } else {
                camelCaseString.append(Character.toLowerCase(currentChar));
            }
        }

        // Ensure the first character is lower case for camelCase
        camelCaseString.setCharAt(0, Character.toLowerCase(camelCaseString.charAt(0)));

        return camelCaseString.toString();
    }
}
