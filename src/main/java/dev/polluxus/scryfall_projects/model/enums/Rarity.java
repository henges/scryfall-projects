package dev.polluxus.scryfall_projects.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public enum Rarity {
    M("mythic"),
    R("rare"),
    U("uncommon"),
    C("common");

    final String rarityName;

    private static final Map<String, Rarity> STRING_TO_RARITY_MAP = EnumSet.allOf(Rarity.class)
            .stream()
            .collect(Collectors.toMap(
                    f -> f.rarityName,
                    f -> f
            ));

    Rarity(String rarityName) {
        this.rarityName = rarityName;
    }

    public static Rarity get(String name) {
        return STRING_TO_RARITY_MAP.get(name);
    }
}
