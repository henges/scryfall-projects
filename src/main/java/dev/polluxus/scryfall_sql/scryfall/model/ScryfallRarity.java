package dev.polluxus.scryfall_sql.scryfall.model;

public enum ScryfallRarity {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    MYTHIC("mythic");

    final String value;

    ScryfallRarity(String value) {
        this.value = value;
    }
}
