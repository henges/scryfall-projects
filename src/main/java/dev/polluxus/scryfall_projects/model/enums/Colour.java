package dev.polluxus.scryfall_projects.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public enum Colour {
    W("W"),
    U("U"),
    B("B"),
    R("R"),
    G("G");

    final String colourName;

    private static final Map<String, Colour> STRING_TO_COLOUR_MAP = EnumSet.allOf(Colour.class)
            .stream()
            .collect(Collectors.toMap(
                    f -> f.colourName,
                    f -> f
            ));

    Colour(String colourName) {
        this.colourName = colourName;
    }

    public static Colour get(String name) {
        return STRING_TO_COLOUR_MAP.get(name);
    }

}
