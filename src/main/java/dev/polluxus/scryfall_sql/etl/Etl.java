package dev.polluxus.scryfall_sql.etl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import dev.polluxus.scryfall_sql.io.Io;
import dev.polluxus.scryfall_sql.processor.Processor;
import dev.polluxus.scryfall_sql.processor.ScryfallCardProcessor;
import dev.polluxus.scryfall_sql.scryfall.model.ScryfallCard;
import dev.polluxus.scryfall_sql.util.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Etl {

    private static final Logger log = LoggerFactory.getLogger(Etl.class);

    private static class Deps {

        MappingIterator<ScryfallCard> iterator;

        Processor<ScryfallCard> processor;

        ExecutorService executor;

        static Deps init(Configuration config) {
            final Deps ret = new Deps();
            ret.iterator = getIterator(config, Io.openReader(config));
            ret.processor = new ScryfallCardProcessor(config);
            return ret;
        }

        static MappingIterator<ScryfallCard> getIterator(Configuration config, Reader reader) {

            try {
                return config.mapper()
                        .readerFor(new TypeReference<ScryfallCard>() {})
                        .readValues(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void run(Configuration config) {

        final Etl.Deps deps = Deps.init(config);
        Etl.process(deps);
    }

    public static void process(Deps deps) {

        final Instant start = Instant.now();

        final MappingIterator<ScryfallCard> it = deps.iterator;
        final Processor<ScryfallCard> processor = deps.processor;

        while (it.hasNext()) { processor.accumulate(it.next()); }

        processor.commit();

        long doneAt = Instant.now().toEpochMilli() - start.toEpochMilli();

        log.info("Done in {}s{}ms", doneAt / 1000, doneAt % 1000);
    }
}
