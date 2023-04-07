package dev.polluxus.scryfall_projects.model.enums;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public enum Format {
    STANDARD("standard"),
    PIONEER("pioneer"),
    MODERN("modern"),
    LEGACY("legacy"),
    VINTAGE("vintage"),
    COMMANDER("commander"),
    ALCHEMY("alchemy"),
    EXPLORER("explorer"),
    BRAWL("brawl"),
    HISTORIC("historic"),
    PAUPER("pauper");

    final String formatName;

    private static final Map<String, Format> STRING_TO_FORMAT_MAP = EnumSet.allOf(Format.class)
                    .stream()
                    .collect(Collectors.toMap(
                            f -> f.formatName,
                            f -> f
                    ));

    Format(String formatName) {
        this.formatName = formatName;
    }

    public static Format get(String name) {
        return STRING_TO_FORMAT_MAP.get(name);
    }
}
