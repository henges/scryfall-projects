package dev.polluxus.scryfall_projects.scryfall.converter;

import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.enums.Game;
import dev.polluxus.scryfall_projects.model.enums.Rarity;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScryfallCardCardEditionConverter implements Converter<ScryfallCard, CardEdition> {

    private static final Logger log = LoggerFactory.getLogger(ScryfallCardCardEditionConverter.class);

    @Override
    public CardEdition convert(ScryfallCard source) {

        final UUID id = source.id();
        final UUID cardId = source.oracleId();
        final String setCode = source.set();
        final String collectorNumber = source.collectorNumber();
        final Rarity rarity = Optional.ofNullable(Rarity.get(source.rarity())).orElseGet(() -> {
            log.trace("Error processing ScryfallCard: " + source);
            return Rarity.C;
        });
        final boolean isReprint = source.reprint();
        final String scryfallUrl = source.scryfallUri();
        final Set<Game> games = source.games()
                .stream()
                .map(Game::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return new CardEdition(
            id,
            cardId,
            setCode,
            collectorNumber,
            rarity,
            isReprint,
            games,
            scryfallUrl
        );
    }
}
