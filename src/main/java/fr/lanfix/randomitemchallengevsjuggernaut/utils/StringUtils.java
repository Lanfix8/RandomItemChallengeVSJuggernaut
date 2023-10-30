package fr.lanfix.randomitemchallengevsjuggernaut.utils;

import java.util.stream.Stream;

public class StringUtils {

    public static String snakeCaseToSpacedPascalCase(String string) {
        StringBuilder result = new StringBuilder();
        String[] words = string.split("_");
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase());
            }
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase());
            }
            result.append(" ");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    public static String streamAsBulletList(Stream<String> stream) {
        StringBuilder result = new StringBuilder();
        stream.forEach(s -> {
            result.append("- ");
            result.append(s);
        });
        return result.toString();
    }

}
