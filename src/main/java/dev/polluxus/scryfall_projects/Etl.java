package dev.polluxus.scryfall_projects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import dev.polluxus.scryfall_projects.cmd.Configuration;
import dev.polluxus.scryfall_projects.processor.Processor;
import dev.polluxus.scryfall_projects.processor.ScryfallCardProcessor;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;
import dev.polluxus.scryfall_projects.util.ExecutorUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
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

        Writer writer;

        static Deps init(Configuration config, Reader reader, Writer writer) {
            final Deps ret = new Deps();
            ret.iterator = getIterator(config, reader);
            ret.executor = getExecutor(config);
            ret.processor = new ScryfallCardProcessor();
            ret.writer = writer;
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

                return ExecutorUtils.directExecutorService();
            }

            return Executors.newFixedThreadPool(config.parallelism());
        }
    }

    public static void run(Configuration config) {

        final Pair<Reader, Writer> rw = IO.openFiles(config);
        final Etl.Deps deps = Deps.init(config, rw.getLeft(), rw.getRight());
        Etl.process(deps);
        IO.closeFiles(rw);
    }

    public static void process(Deps deps) {

        final MappingIterator<ScryfallCard> it = deps.iterator;
        final ExecutorService executor = deps.executor;
        final Processor<ScryfallCard> processor = deps.processor;

        final int batchSize = 50;
        final ScryfallCard[] buf = new ScryfallCard[batchSize];

        int i = 0;
        while (it.hasNext()) {

            buf[i++] = it.next();
            if (i == batchSize) {

                log.info("Thread name in process: {}", Thread.currentThread().getName());

                final List<ScryfallCard> copy = Arrays.asList(buf);
                executor.submit(() ->
                    processor.process(copy)
                );
                i = 0;
            }
        }

        // We're out of cards. Check if there's any cards left in the array.
        if (i != 0) {

            // Retrieve only the cards we care about
            final ScryfallCard[] copy = Arrays.copyOf(buf, i);
            executor.submit(() ->
                processor.process(Arrays.asList(copy))
            );
        }

        try {
            executor.shutdown();
            boolean passed = executor.awaitTermination(300, TimeUnit.SECONDS);
            if (!passed) {
                throw new RuntimeException("DNF");
            }
            processor.commit(deps.writer);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Done!
    }
}
