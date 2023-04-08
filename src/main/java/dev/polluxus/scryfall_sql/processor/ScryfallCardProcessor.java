package dev.polluxus.scryfall_sql.processor;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.MagicSet;
import dev.polluxus.scryfall_sql.model.enums.Format;
import dev.polluxus.scryfall_sql.io.writer.EtlWriter;
import dev.polluxus.scryfall_sql.scryfall.converter.ScryfallCardCardConverter;
import dev.polluxus.scryfall_sql.scryfall.converter.ScryfallCardCardEditionConverter;
import dev.polluxus.scryfall_sql.scryfall.converter.ScryfallCardMagicSetConverter;
import dev.polluxus.scryfall_sql.scryfall.model.ScryfallCard;
import dev.polluxus.scryfall_sql.util.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScryfallCardProcessor implements Processor<ScryfallCard> {

    private static final Logger log = LoggerFactory.getLogger(ScryfallCardProcessor.class);

    private final ScryfallCardCardConverter cardConverter = new ScryfallCardCardConverter();
    private final ScryfallCardCardEditionConverter editionConverter = new ScryfallCardCardEditionConverter();
    private final ScryfallCardMagicSetConverter setConverter = new ScryfallCardMagicSetConverter();

    private final ConcurrentMap<String, Boolean> setLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Boolean> cardLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> editionLocks = new ConcurrentHashMap<>();

    private final EtlWriter writer;
    private final ExecutorService executor;

    static class Metrics {
        int elementsSubmitted;
        AtomicInteger elementsRejected = new AtomicInteger();
        AtomicInteger elementsAccepted = new AtomicInteger();
    }
    final Metrics metrics = new Metrics();

    public ScryfallCardProcessor(Configuration config) {
        this.writer = config.writer();
        this.executor = ExecutorUtils.getExecutor(config);
        buf = new ScryfallCard[config.batchSize()];
    }

    private final ScryfallCard[] buf;
    private int i = 0;

    public void accumulate(ScryfallCard element) {

        buf[i++] = element;
        if (i == buf.length) {

            submitBatch();
        }
    }

    private void submitBatch() {

        final List<ScryfallCard> copy = Arrays.asList(Arrays.copyOf(buf, i));
        metrics.elementsSubmitted += i;
        executor.submit(() -> process(copy));
        i = 0;
    }

    @Override
    public void commit() {

        // We've finished reading data. If the dataset size
        // is not a multiple of the batch size, submit the
        // final undersized batch.
        if (i != 0) { submitBatch(); }

        try {
            executor.shutdown();
            boolean passed = executor.awaitTermination(300, TimeUnit.SECONDS);
            if (!passed) {
                throw new RuntimeException("DNF");
            }
        }  catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.info("Final count: sets {}, cards {}, editions {}; elements submitted {}, elements accepted {}, elements rejected {}",
                setLocks.size(), cardLocks.size(), editionLocks.size(), metrics.elementsSubmitted, metrics.elementsAccepted.get(),
                metrics.elementsRejected.get());
        writer.commit();
    }

    @Override
    public void process(List<ScryfallCard> elements) {

        final List<MagicSet> sets = new ArrayList<>();
        final List<Card> cards = new ArrayList<>();
        final List<CardEdition> editions = new ArrayList<>();

        for (var e : elements) {

            if (!Format.hasValidFormat(e.legalities()) || e.games().isEmpty() || e.oracleId() == null) {
                log.trace("Skipping invalid card {}", e);
                metrics.elementsRejected.incrementAndGet();
                continue;
            }

            boolean any = false;

            if (setLocks.putIfAbsent(e.set(), true) == null) {
                sets.add(setConverter.convert(e));
                any = true;
            }
            if (cardLocks.putIfAbsent(e.oracleId(), true) == null) {
                cards.add(cardConverter.convert(e));
                any = true;
            }
            if (editionLocks.putIfAbsent(e.set() + "~" + e.collectorNumber(), true) == null) {
                editions.add(editionConverter.convert(e));
                any = true;
            }

            if (any) {
                metrics.elementsAccepted.incrementAndGet();
            }
        }

        writer.write(sets, cards, editions);
    }
}
