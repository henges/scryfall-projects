package dev.polluxus.scryfall_projects.scryfall.converter;

import dev.polluxus.scryfall_projects.model.Card;
import dev.polluxus.scryfall_projects.model.Card.CardFace;
import dev.polluxus.scryfall_projects.model.enums.Format;
import dev.polluxus.scryfall_projects.model.enums.Game;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScryfallCardCardConverter implements Converter<ScryfallCard, Card> {

    private final ScryfallCardCardFaceConverter cardToCardFace = new ScryfallCardCardFaceConverter();
    private final ScryfallCardFaceCardFaceConverter cardFaceToCardFace = new ScryfallCardFaceCardFaceConverter();

    @Override
    public Card convert(ScryfallCard source) {

        final UUID cardId = source.oracleId();

        final String name = source.name();

        final Set<Format> formats = source.legalities()
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals("legal"))
                .map(e -> Format.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Set<Game> games = source.games()
                .stream()
                .map(Game::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final List<CardFace> faces;

        // Do we have card faces? If so, get *most* card info from there.
        if (source.cardFaces() != null) {

            faces = source.cardFaces()
                    .stream()
                    .map(f -> cardFaceToCardFace.convert(Pair.of(source, f)))
                    .toList();
        } else {

            faces = List.of(cardToCardFace.convert(source));
        }

        return new Card(
            cardId,
            name,
            faces,
            formats,
            games
        );
    }
}
