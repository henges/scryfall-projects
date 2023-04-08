package dev.polluxus.scryfall_sql.io.writer;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.io.Io;
import dev.polluxus.scryfall_sql.io.format.EtlFormat;
import dev.polluxus.scryfall_sql.io.format.SqlFormat;
import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.MagicSet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class InMemoryEtlWriter implements EtlWriter {

    private final Deque<String> sets = new ConcurrentLinkedDeque<>();
    private final Deque<String> cards = new ConcurrentLinkedDeque<>();
    private final Deque<String> editions = new ConcurrentLinkedDeque<>();

    private final Writer outputWriter;
    private final EtlFormat format;

    public InMemoryEtlWriter(Configuration config) {

        this.outputWriter = Io.openWriter(config);
        this.format = config.format();
    }

    @Override
    public void write(Collection<MagicSet> sets, Collection<Card> cards, Collection<CardEdition> editions) {

        this.sets.addAll(format.sets(sets));
        this.cards.addAll(format.cards(cards));
        this.editions.addAll(format.editions(editions));
    }

    @Override
    public void commit() {

        format.start().ifPresent(s -> Io.writeString(outputWriter, s));

        sets.forEach(s -> Io.writeString(outputWriter, s));
        cards.forEach(s -> Io.writeString(outputWriter, s));
        editions.forEach(s -> Io.writeString(outputWriter, s));

        format.end().ifPresent(s -> Io.writeString(outputWriter, s));

        Io.flushAndClose(outputWriter);
    }
}
