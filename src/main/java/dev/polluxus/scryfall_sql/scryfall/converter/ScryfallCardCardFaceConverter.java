package dev.polluxus.scryfall_sql.scryfall.converter;

import dev.polluxus.scryfall_sql.model.Card.CardFace;
import dev.polluxus.scryfall_sql.model.enums.Colour;
import dev.polluxus.scryfall_sql.scryfall.model.ScryfallCard;
import dev.polluxus.scryfall_sql.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScryfallCardCardFaceConverter implements Converter<ScryfallCard, CardFace> {

    @Override
    public CardFace convert(ScryfallCard source) {

        final UUID cardId = source.oracleId();
        final String name = source.name();
        final int manaValue = (int) source.cmc();
        final List<String> manaCost = StringUtils.parseManaCost(source.manaCost());
        final String oracleText = source.oracleText();
        final String power = source.power();
        final String toughness = source.toughness();
        final String loyalty = source.loyalty();
        final Set<Colour> colours = StringUtils.parseColours(source.colors());

        final Pair<List<String>, List<String>> types = StringUtils.parseCardTypes(source.typeLine());

        return new CardFace(
                cardId, name, manaValue, manaCost, colours, types.getLeft(), types.getRight(), oracleText, power, toughness, loyalty
        );
    }
}
