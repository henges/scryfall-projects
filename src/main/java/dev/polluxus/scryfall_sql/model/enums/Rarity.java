package dev.polluxus.scryfall_sql.model.enums;

import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum Rarity {
    M("mythic"),
    R("rare"),
    U("uncommon"),
    C("common"),
    S("special", "bonus");

    final Set<String> rarityNames;

    private static final Map<String, Rarity> STRING_TO_RARITY_MAP = EnumSet.allOf(Rarity.class)
            .stream()
            .flatMap(r -> r.rarityNames.stream().map(n -> Pair.of(n, r)))
            .collect(Collectors.toMap(
                    Pair::getLeft,
                    Pair::getRight
            ));

    Rarity(String... rarityNames) {
        this.rarityNames = Set.of(rarityNames);
    }

    public static Rarity get(String name) {
        return STRING_TO_RARITY_MAP.get(name);
    }
}
