package dev.polluxus.scryfall_projects.scryfall.converter;

import dev.polluxus.scryfall_projects.model.Card.CardFace;
import dev.polluxus.scryfall_projects.model.enums.Colour;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard.ScryfallCardFace;
import dev.polluxus.scryfall_projects.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScryfallCardCardFaceConverter implements Converter<ScryfallCard, CardFace> {

    @Override
    public CardFace convert(ScryfallCard source) {

        final UUID cardId = source.oracleId();
        final String name = source.name();
        final int manaValue = source.cmc();
        final List<String> manaCost = StringUtils.parseManaCost(source.manaCost());
        final List<String> cardTypes = StringUtils.parseCardTypes(source.typeLine());
        final String oracleText = source.oracleText();
        final String power = source.power();
        final String toughness = source.toughness();
        final String loyalty = source.loyalty();

        final Set<Colour> colourIdentity = StringUtils.parseColourIdentity(manaCost);

        return new CardFace(
                cardId, name, manaValue, manaCost, cardTypes, oracleText, power, toughness, loyalty, colourIdentity
        );
    }
}
