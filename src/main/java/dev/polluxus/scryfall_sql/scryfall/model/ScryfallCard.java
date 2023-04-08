package dev.polluxus.scryfall_sql.scryfall.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Date;
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
        String set,
        @JsonProperty("set_name") String setName,
        @JsonProperty("collector_number") String collectorNumber,
        @JsonProperty("scryfall_uri") String scryfallUri,
        @JsonProperty("released_at") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") Date releasedAt,
        // Per-face properties
        String name,
        @JsonProperty("mana_cost") String manaCost,
        double cmc,
        @JsonProperty("type_line") String typeLine,
        @JsonProperty("oracle_text") String oracleText,
        String power,
        String toughness,
        String loyalty,
        List<String> colors,
        @JsonProperty("color_identity") List<String> colorIdentity,
        // May be empty
        List<String> keywords,
        @JsonProperty("card_faces") List<ScryfallCardFace> cardFaces
) {

    public record ScryfallCardFace(
            String name,
            @JsonProperty("mana_cost") String manaCost,
            @JsonProperty("type_line") String typeLine,
            @JsonProperty("oracle_text") String oracleText,
            @JsonProperty("color_indicator") List<String> colorIndicator,
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
