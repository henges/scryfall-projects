package dev.polluxus.scryfall_projects.scryfall.model;

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
