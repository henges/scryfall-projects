package dev.polluxus.scryfall_projects.model;

import dev.polluxus.scryfall_projects.model.enums.Colour;
import dev.polluxus.scryfall_projects.model.enums.Format;
import dev.polluxus.scryfall_projects.model.enums.Game;
import dev.polluxus.scryfall_projects.model.enums.Rarity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record Card(
    UUID id,
    String name,
    List<CardFace> faces,
    Set<Format> formats,
    Set<Game> games,
    Set<Colour> colourIdentity,
    Set<String> keywords
) {

    public record CardFace(
       UUID cardId,
       String name,
       int manaValue,
       List<String> manaCost,
       Set<Colour> colours,
       List<String> types,
       List<String> subtypes,
       String oracleText,
       String power,
       String toughness,
       String loyalty
    ) {}

    public record CardEdition(
       UUID id,
       UUID cardId,
       String setCode,
       String collectorNumber,
       Rarity rarity,
       boolean isReprint,
       String scryfallUrl
    ) {}
}
