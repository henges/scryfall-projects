package dev.polluxus.scryfall_sql.model.enums;

import java.util.*;
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

    private static final Set<String> VALID_FORMATS = STRING_TO_FORMAT_MAP.keySet();

    Format(String formatName) {
        this.formatName = formatName;
    }

    public static Format get(String name) {
        return STRING_TO_FORMAT_MAP.get(name);
    }

    public static boolean hasValidFormat(Map<String, String> formats) {

        return formats.entrySet()
                .stream()
                .filter(e -> VALID_FORMATS.contains(e.getKey()))
                .filter(e -> e.getValue().equals("legal"))
                .toList().size() > 0;
    }

}
