package dev.polluxus.scryfall_projects.util;

import dev.polluxus.scryfall_projects.model.enums.Colour;

import java.util.*;
import java.util.stream.Collectors;

public class StringUtils {

    private StringUtils () {}

    public static String quotedStringOrNullLiteral(final String s) {

        if (s == null) {
            return "NULL";
        }

        return "'" + s + "'";
    }

    public static String escapeQuotes(final String string) {

        return string.replace("'", "\\'");
    }

    public static String delimitedString(final Collection<?> objs) {

        return delimitedString(objs, false);
    }

    public static String delimitedString(final Collection<?> objs, final boolean escape) {

        final String delimiter, startToken;
        if (escape) {
            delimiter = "',E'";
            startToken = "E'";
        } else {
            delimiter = "','";
            startToken = "'";
        }

        return startToken + objs.stream()
                .map(Object::toString)
                .map(s -> escape ? escapeQuotes(s) : s)
                .collect(Collectors.joining(delimiter)) + "'";
    }

    public static List<String> parseManaCost(final String manaCost) {
        return Arrays.stream(manaCost.split("}\\{"))
                .map(s -> s.replace("{", "").replace("}", ""))
                .toList();
    }

    public static List<String> parseCardTypes(final String typeLine) {
        return Arrays.stream(typeLine.split("(\s)"))
                .filter(s -> s != null && !s.equals("") && !s.equals("—") && !s.equals("-"))
                .toList();
    }

    public static Set<Colour> parseColourIdentity(final List<String> costComponents) {

        final Set<Colour> result = costComponents.stream()
                // Hybrid mana costs contribute to colour identity
                .flatMap(s -> Arrays.stream(s.split("/")))
                .map(Colour::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (result.isEmpty()) {
            return Set.of(Colour.COLOURLESS);
        }

        return result;
    }
}
