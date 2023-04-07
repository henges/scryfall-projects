package dev.polluxus.scryfall_projects.util;

import dev.polluxus.scryfall_projects.model.enums.Colour;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StringUtils {

    private StringUtils () {}

    public static List<String> parseManaCost(final String manaCost) {
        return Arrays.stream(manaCost.split("}\\{"))
                .map(s -> s.replace("{", "").replace("}", ""))
                .toList();
    }

    public static List<String> parseCardTypes(final String typeLine) {
        return Arrays.stream(typeLine.split("(\s-\s|\s)")).toList();
    }

    public static Set<Colour> parseColourIdentity(final List<String> costComponents) {

        return costComponents.stream()
                // Hybrid mana costs contribute to colour identity
                .flatMap(s -> Arrays.stream(s.split("/")))
                .map(Colour::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
