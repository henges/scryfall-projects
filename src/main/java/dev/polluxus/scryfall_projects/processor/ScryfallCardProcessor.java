package dev.polluxus.scryfall_projects.processor;

import dev.polluxus.scryfall_projects.model.Card;
import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.MagicSet;
import dev.polluxus.scryfall_projects.model.enums.Format;
import dev.polluxus.scryfall_projects.io.EtlWriter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardCardConverter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardCardEditionConverter;
import dev.polluxus.scryfall_projects.scryfall.converter.ScryfallCardMagicSetConverter;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ScryfallCardProcessor implements Processor<ScryfallCard> {

    private static final Logger log = LoggerFactory.getLogger(ScryfallCardProcessor.class);

    private final ScryfallCardCardConverter cardConverter = new ScryfallCardCardConverter();
    private final ScryfallCardCardEditionConverter editionConverter = new ScryfallCardCardEditionConverter();
    private final ScryfallCardMagicSetConverter setConverter = new ScryfallCardMagicSetConverter();

    private final ConcurrentMap<String, Boolean> setLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Boolean> cardLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> editionLocks = new ConcurrentHashMap<>();

    private final EtlWriter format;

    public ScryfallCardProcessor(EtlWriter format) {
        this.format = format;
    }

    @Override
    public void start() {
        format.start();
    }

    @Override
    public void commit() {
        format.end();
    }

    @Override
    public void process(List<ScryfallCard> elements) {

        final List<MagicSet> sets = new ArrayList<>();
        final List<Card> cards = new ArrayList<>();
        final List<CardEdition> editions = new ArrayList<>();

        for (var e : elements) {

            if (!Format.hasValidFormat(e.legalities()) || e.games().isEmpty() || e.oracleId() == null) {
                log.trace("Skipping invalid card {}", e);
                continue;
            }

            if (setLocks.putIfAbsent(e.set(), true) == null) {
                sets.add(setConverter.convert(e));
            }
            if (cardLocks.putIfAbsent(e.oracleId(), true) == null) {
                cards.add(cardConverter.convert(e));
            }
            if (editionLocks.putIfAbsent(e.set() + "~" + e.collectorNumber(), true) == null) {
                editions.add(editionConverter.convert(e));
            }
        }

        format.write(sets, cards, editions);
    }
}
