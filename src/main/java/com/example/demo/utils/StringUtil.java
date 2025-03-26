package com.example.demo.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class StringUtil {
    public static String urlConverter(String subDirectory, String toReplace, String replaceWith) {
        return subDirectory.replaceAll(toReplace, replaceWith);
    }

    public static String splitByCharacterTypeCamelCase(String name) {
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase
                (name.replaceAll("\\d+", "")), " ");
    }

    public static String toNormalCase(String text) {

        final char[] delimiters = {' ', '_'};

        String normalCase = WordUtils.capitalizeFully(text, delimiters);
        return normalCase;
    }

    public static String toUpperCase(String name) {
        return name.toUpperCase();
    }
}
