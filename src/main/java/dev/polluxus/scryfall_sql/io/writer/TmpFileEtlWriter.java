package dev.polluxus.scryfall_sql.io.writer;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.io.Io;
import dev.polluxus.scryfall_sql.io.format.EtlFormat;
import dev.polluxus.scryfall_sql.io.format.SqlFormat;
import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.MagicSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Uses temporary files as the backing store for the accumulators.
 */
public class TmpFileEtlWriter implements EtlWriter {

    private static final Logger log = LoggerFactory.getLogger(TmpFileEtlWriter.class);

    private final EtlFormat format;

    private final Writer outputWriter;

    private final Writer setWriter;
    private final Writer cardWriter;
    private final Writer editionWriter;

    private final Path setPath;
    private final Path cardPath;
    private final Path editionPath;

    public TmpFileEtlWriter(Configuration config) {

        this.format = new SqlFormat();
        this.outputWriter = Io.openWriter(config);
        var sets = Io.writerForTempFile("sets");
        var cards = Io.writerForTempFile("cards");
        var editions = Io.writerForTempFile("editions");
        setWriter = sets.getLeft();
        cardWriter = cards.getLeft();
        editionWriter = editions.getLeft();
        setPath = sets.getRight();
        cardPath = cards.getRight();
        editionPath = editions.getRight();
    }

    @Override
    public void start() {
        // Noop
    }

    @Override
    public void end() {

        try {
            log.info("Beginning final write");

            // Close the writers for the temp files
            Io.flushAndClose(setWriter, cardWriter, editionWriter);

            // Open readers for each temp file
            Reader setReader = Io.openReader(setPath);
            Reader cardReader = Io.openReader(cardPath);
            Reader editionReader = Io.openReader(editionPath);

            format.start().ifPresent(s -> Io.writeString(outputWriter, s));

            setReader.transferTo(outputWriter);
            cardReader.transferTo(outputWriter);
            // Write editions last since they have set/card FKs
            editionReader.transferTo(outputWriter);

            format.end().ifPresent(s -> Io.writeString(outputWriter, s));

            Io.close(setReader, cardReader, editionReader);

            Files.delete(setPath);
            Files.delete(cardPath);
            Files.delete(editionPath);

            Io.flushAndClose(outputWriter);

            log.info("Results written successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void write(Collection<MagicSet> sets, Collection<Card> cards, Collection<CardEdition> editions) {

        synchronized (setWriter) {
            sets.forEach(s -> Io.writeString(setWriter, format.set(s)));
        }
        synchronized (cardWriter) {
            cards.forEach(s -> Io.writeString(cardWriter, format.card(s)));
        }
        synchronized (editionWriter) {
            editions.forEach(s -> Io.writeString(editionWriter, format.edition(s)));
        }
    }
}
