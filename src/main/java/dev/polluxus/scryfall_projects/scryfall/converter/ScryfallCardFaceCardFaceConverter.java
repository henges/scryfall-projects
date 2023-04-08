package dev.polluxus.scryfall_projects.scryfall.converter;

import dev.polluxus.scryfall_projects.model.Card.CardFace;
import dev.polluxus.scryfall_projects.model.enums.Colour;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard.ScryfallCardFace;
import dev.polluxus.scryfall_projects.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ScryfallCardFaceCardFaceConverter implements Converter<Pair<ScryfallCard, ScryfallCardFace>, CardFace> {

    private static final Logger log = LoggerFactory.getLogger(ScryfallCardFaceCardFaceConverter.class);

    @Override
    public CardFace convert(Pair<ScryfallCard, ScryfallCardFace> source) {

        final ScryfallCard card = source.getLeft();
        final ScryfallCardFace face = source.getRight();

        final UUID cardId = card.oracleId();
        final String name = face.name();
        final int manaValue = (int) card.cmc();
        final List<String> manaCost = StringUtils.parseManaCost(face.manaCost());
        final String oracleText = face.oracleText();
        final String power = face.power();
        final String toughness = face.toughness();
        final String loyalty = face.loyalty();
        final Set<Colour> colours;

        if (face.colors() != null) {
            colours = StringUtils.parseColours(face.colors());
        } else {
            log.trace("Card face with no colours: {}", face);
            colours = StringUtils.parseColours(card.colors());
        }

        final Pair<List<String>, List<String>> types = StringUtils.parseCardTypes(face.typeLine());

        return new CardFace(
                cardId, name, manaValue, manaCost, colours, types.getLeft(), types.getRight(), oracleText, power, toughness, loyalty
        );
    }
}
