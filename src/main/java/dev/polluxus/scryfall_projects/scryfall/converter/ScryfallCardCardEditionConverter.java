package dev.polluxus.scryfall_projects.scryfall.converter;

import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.enums.Rarity;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;

import java.util.UUID;

public class ScryfallCardCardEditionConverter implements Converter<ScryfallCard, CardEdition> {

    @Override
    public CardEdition convert(ScryfallCard source) {

        final UUID id = source.id();
        final UUID cardId = source.oracleId();
        final String setCode = source.set();
        final String collectorNumber = source.collectorNumber();
        final Rarity rarity = Rarity.get(source.rarity());
        final boolean isReprint = source.reprint();
        final String scryfallUrl = source.scryfallUri();

        return new CardEdition(
            id,
            cardId,
            setCode,
            collectorNumber,
            rarity,
            isReprint,
            scryfallUrl
        );
    }
}
