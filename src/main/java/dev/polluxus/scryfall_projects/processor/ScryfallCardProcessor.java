package dev.polluxus.scryfall_projects.processor;

import dev.polluxus.scryfall_projects.model.Card;
import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.MagicSet;
import dev.polluxus.scryfall_projects.model.enums.Format;
import dev.polluxus.scryfall_projects.output.OutputFormat;
import dev.polluxus.scryfall_projects.output.SqlOutputFormat;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardCardConverter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardCardEditionConverter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardMagicSetConverter;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ScryfallCardProcessor implements Processor<ScryfallCard> {

    private static final Logger log = LoggerFactory.getLogger(ScryfallCardProcessor.class);

    private final ScryfallCardCardConverter cardConverter = new ScryfallCardCardConverter();
    private final ScryfallCardCardEditionConverter editionConverter = new ScryfallCardCardEditionConverter();
    private final ScryfallCardMagicSetConverter setConverter = new ScryfallCardMagicSetConverter();

    private final ConcurrentMap<String, MagicSet> sets = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Card> cards = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CardEdition> editions = new ConcurrentHashMap<>();

    @Override
    public void commit(Writer writer) {
        OutputFormat format = new SqlOutputFormat(writer);
        format.output(sets.values(), cards.values(), editions.values());
    }

    @Override
    public void process(List<ScryfallCard> elements) {

        log.info("Thread name in process: {}", Thread.currentThread().getName());

        for (var e : elements) {

            if (!Format.hasValidFormat(e.legalities()) || e.games().isEmpty() || e.oracleId() == null) {
                log.trace("Skipping invalid card " + e.name());
                continue;
            }
            sets.computeIfAbsent(e.set(), __ -> setConverter.convert(e));
            cards.computeIfAbsent(e.oracleId(), __ -> cardConverter.convert(e));
            editions.computeIfAbsent(e.set() + "~" + e.collectorNumber(), __ -> editionConverter.convert(e));
        }
    }
}
