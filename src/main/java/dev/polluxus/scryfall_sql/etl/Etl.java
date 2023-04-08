package dev.polluxus.scryfall_sql.etl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import dev.polluxus.scryfall_sql.io.Io;
import dev.polluxus.scryfall_sql.io.writer.EtlWriter;
import dev.polluxus.scryfall_sql.io.writer.InMemoryEtlWriter;
import dev.polluxus.scryfall_sql.io.writer.TmpFileEtlWriter;
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
import java.util.concurrent.Executors;
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
            ret.executor = getExecutor(config);
            ret.processor = new ScryfallCardProcessor(config.writer());
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

        static ExecutorService getExecutor(Configuration config) {

            if (config.parallelism().equals(1)) {

                log.info("Running with parallelism of 1 (disabled)");
                return ExecutorUtils.directExecutorService();
            }

            log.warn("Running with parallelism of {}", config.parallelism());

            return Executors.newFixedThreadPool(config.parallelism());
        }
    }

    public static void run(Configuration config) {

        final Etl.Deps deps = Deps.init(config);
        Etl.process(deps, config.batchSize());
    }

    public static void process(Deps deps, int batchSize) {

        final Instant start = Instant.now();

        final MappingIterator<ScryfallCard> it = deps.iterator;
        final ExecutorService executor = deps.executor;
        final Processor<ScryfallCard> processor = deps.processor;

        final ScryfallCard[] buf = new ScryfallCard[batchSize];

        int i = 0;
        while (it.hasNext()) {

            buf[i++] = it.next();
            if (i == batchSize) {

                final List<ScryfallCard> copy = Arrays.asList(Arrays.copyOf(buf, batchSize));
                executor.submit(() -> processor.process(copy));
                i = 0;
            }
        }

        // We're out of cards. Check if there's any cards left in the array.
        if (i != 0) {

            // Retrieve only the cards we care about
            final ScryfallCard[] copy = Arrays.copyOf(buf, i);
            executor.submit(() -> processor.process(Arrays.asList(copy)));
        }

        try {
            executor.shutdown();
            boolean passed = executor.awaitTermination(300, TimeUnit.SECONDS);
            if (!passed) {
                throw new RuntimeException("DNF");
            }
            processor.commit();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long doneAt = Instant.now().toEpochMilli() - start.toEpochMilli();

        log.info("Done in {}s{}ms", doneAt / 1000, doneAt % 1000);
    }
}
