package dev.polluxus.scryfall_sql.etl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.polluxus.scryfall_sql.io.writer.EtlWriter;
import dev.polluxus.scryfall_sql.io.writer.InMemoryEtlWriter;
import dev.polluxus.scryfall_sql.io.writer.TmpFileEtlWriter;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class Configuration {

    private final ObjectMapper mapper;

    private final String inputPath;

    private final String outputPath;

    private final String parallelism;

    private final String batchSize;

    private final String writer;

    public Configuration(
            String inputPath,
            String outputPath,
            String parallelism,
            String batchSize,
            String writer
    ) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.parallelism = parallelism;
        this.batchSize = batchSize;
        this.writer = writer;

        this.mapper = initMapper();
    }

    private ObjectMapper initMapper() {

        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public Path inputPath() {
        if (inputPath == null) {
            return Path.of("src/main/resources/card_data/default-cards-20230403090602.json");
        }
        return Path.of(inputPath);
    }

    public Path outputPath() {
        if (outputPath == null) {
            return Path.of("output/output-" + Instant.now().getEpochSecond() + ".sql");
        }
        return Path.of(outputPath);
    }

    final static int MAX_PARALLELISM = 100;

    public Integer parallelism() {
        if (parallelism == null) {
            return 1;
        }
        return Math.min(Integer.parseInt(parallelism), MAX_PARALLELISM);
    }

    public Integer batchSize() {
        if (batchSize == null) {
            return 50;
        }
        return Integer.parseInt(batchSize);
    }

    public EtlWriter writer() {

        return switch (writer) {
            case "IN_MEMORY" -> new InMemoryEtlWriter(this);
            case "TMP_FILE" -> new TmpFileEtlWriter(this);
            default -> new TmpFileEtlWriter(this);
        };
    }
}
