package dev.polluxus.scryfall_projects.scryfall.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ScryfallCard(
        // Properties common to all faces and editions
        @JsonProperty("oracle_id") UUID oracleId, // Shared card ID
        Map<String, String> legalities,
        List<String> games,
        // Per-edition properties
        UUID id, // Unique printing ID
        String rarity,
        boolean reprint,
        @JsonProperty("collectorNumber") String collectorNumber,
        @JsonProperty("scryfall_uri") String scryfallUri,
        // Per-face properties
        String name,
        @JsonProperty("mana_cost") String manaCost,
        int cmc,
        @JsonProperty("type_line") String typeLine,
        @JsonProperty("oracle_text") String oracleText,
        String power,
        String toughness,
        String loyalty,
        List<String> colors,
        @JsonProperty("color_identity") List<String> colorIdentity,
        // May be empty
        @JsonProperty("card_faces") List<ScryfallCardFace> cardFaces
) {

    public record ScryfallCardFace(
            String name,
            @JsonProperty("mana_cost") String manaCost,
            @JsonProperty("type_line") String typeLine,
            @JsonProperty("oracle_text") String oracleText,
            String power,
            String toughness,
            String loyalty,
            List<String> colors
            // No color identity
            // No cmc
    ) {

        public int cmc() {
            // TODO
            return 0;
        }

        public String colorIdentity() {
            // TODO
            return null;
        }
    }
}
