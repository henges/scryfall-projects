package dev.polluxus.scryfall_projects.processor;

import dev.polluxus.scryfall_projects.model.Card;
import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.MagicSet;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardCardConverter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardCardEditionConverter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardMagicSetConverter;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;

import java.io.Writer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScryfallSqlProcessor implements Processor<ScryfallCard> {

    private final ConcurrentHashMap<UUID, Boolean> processedOracleIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> processedSets = new ConcurrentHashMap<>();
    private final ScryfallCardCardConverter cardConverter = new ScryfallCardCardConverter();
    private final ScryfallCardCardEditionConverter editionConverter = new ScryfallCardCardEditionConverter();
    private final ScryfallCardMagicSetConverter setConverter = new ScryfallCardMagicSetConverter();

    @Override
    public void process(List<ScryfallCard> elements, Writer writer) {

        for (var e : elements) {

            // Avoid processing oracle IDs and sets we've already processed.
            boolean setExists = processedSets.putIfAbsent(e.set(), true) != null;
            if (!setExists) {
                final MagicSet set = setConverter.convert(e);
            }
            boolean cardExists = processedOracleIds.putIfAbsent(e.oracleId(), true) != null;
            if (!cardExists) {
                final Card card = cardConverter.convert(e);
            }
            final CardEdition edition = editionConverter.convert(e);

        }

    }
}
