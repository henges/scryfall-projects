package dev.polluxus.scryfall_projects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.polluxus.scryfall_projects.processor.Processor;
import dev.polluxus.scryfall_projects.processor.ScryfallSqlProcessor;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setDateFormat(df)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        try {
            final var fileReader = Files.newBufferedReader(Path.of("src/main/resources/card_data/default-cards-20230403090602.json"));
            final var reader = mapper.readerFor(new TypeReference<ScryfallCard>() {}).readValues(fileReader);
            final FileWriter writer = new FileWriter("output/output-" + Instant.now().getEpochSecond() + ".sql");
            process(reader, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void process(MappingIterator<Object> iterator, FileWriter writer) throws IOException {

        final ExecutorService executor = Executors.newFixedThreadPool(10);
        final int batchSize = 50;
        final ScryfallCard[] buf = new ScryfallCard[batchSize];
        final Processor<ScryfallCard> processor = new ScryfallSqlProcessor();

        int i = 0;
        while (iterator.hasNext()) {

            buf[i++] = (ScryfallCard) iterator.next();
            if (i == batchSize) {

                final List<ScryfallCard> copy = Arrays.asList(buf);
//                executor.submit(() ->
                        processor.process(copy, writer)
//                )
                ;
                i = 0;
            }
        }

        // We're out of cards. Check if there's any cards left in the array.
        if (i != 0) {

            // Retrieve only the cards we care about
            final ScryfallCard[] copy = Arrays.copyOf(buf, i);
//            executor.submit(() ->
                    processor.process(Arrays.asList(copy), writer)
//            )
            ;
        }

        try {
            executor.shutdown();
            boolean passed = executor.awaitTermination(300, TimeUnit.SECONDS);
            if (!passed) {
                throw new RuntimeException("DNF");
            }
            processor.commit(writer);
            writer.flush();
            writer.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Done!
    }
}
