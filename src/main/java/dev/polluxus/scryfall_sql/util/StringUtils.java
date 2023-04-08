package dev.polluxus.scryfall_sql.util;

import dev.polluxus.scryfall_sql.model.enums.Colour;
import org.apache.commons.lang3.tuple.Pair;

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
                .map(s -> s.replaceAll("[{}]", ""))
                .toList();
    }

    public static Pair<List<String>, List<String>> parseCardTypes(final String typeLine) {

        String[] typesArr = typeLine.split("\s[—-]\s");
        final List<String> types = new ArrayList<>();
        final List<String> subtypes = new ArrayList<>();
        if (typesArr.length >= 1) {
            types.addAll(Arrays.asList(typesArr[0].split("\s+")));
        }
        if (typesArr.length == 2) {
            subtypes.addAll(Arrays.asList(typesArr[1].split("\s+")));
        }

        return Pair.of(types, subtypes);
    }

    public static Set<Colour> parseColours(final List<String> colors) {

        final Set<Colour> result = colors.stream()
                // Hybrid mana costs contribute to colour identity
                .map(Colour::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (result.isEmpty()) {
            return Set.of(Colour.COLOURLESS);
        }

        return result;
    }
}
