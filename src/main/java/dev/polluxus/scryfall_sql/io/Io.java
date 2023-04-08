package dev.polluxus.scryfall_sql.io;

import dev.polluxus.scryfall_sql.etl.Configuration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Io {

    private static final Logger log = LoggerFactory.getLogger(Io.class);

    public static Writer openWriter(Configuration config) {

        try {
            log.info("Opening writer at path {}", config.outputPath());
            return Files.newBufferedWriter(config.outputPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Reader openReader(Configuration config) {

        return openReader(config.inputPath());
    }

    public static Reader openReader(Path path) {

        try {
            log.info("Opening reader at path {}", path);
            return Files.newBufferedReader(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void close(Reader... readers) {
        for (var r : readers) {
            try {
                r.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void flushAndClose(Writer... writers) {

        for (var w : writers) {
            try {
                w.flush();
                w.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Pair<Writer, Path> writerForTempFile(final String name) {

        try {
            Path p = Files.createTempFile(name, null);
            log.info("Created temp file with path {}", p);
            return Pair.of(new BufferedWriter(new FileWriter(p.toFile())), p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeString(Writer writer, String string) {

        try {
            writer.write(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
