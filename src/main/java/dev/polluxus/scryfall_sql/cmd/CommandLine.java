package dev.polluxus.scryfall_sql.cmd;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.etl.Etl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

public class CommandLine {

    private static final Logger log = LoggerFactory.getLogger(CommandLine.class);

    public static void main(String[] args) {

        log.trace(Arrays.toString(args));

        Configuration config = fromArgs(args);

        Etl.run(config);
    }

    private static Configuration fromArgs(String[] args) {

        Iterator<String> it = Arrays.asList(args).iterator();
        String inputPath = null,
                outputPath = null,
                parallelism = null,
                batchSize = null,
                writer = null,
                format = null;

        while (it.hasNext()) {
            final String s = it.next();
            switch (s) {
                case "-in" -> inputPath = readOrFail(it, "-in");
                case "-out" -> outputPath = readOrFail(it, "-out");
                case "-p" -> parallelism = readOrFail(it, "-p");
                case "-b" -> batchSize = readOrFail(it, "-b");
                case "-writer" -> writer = readOrFail(it, "-writer");
                case "-format" -> format = readOrFail(it, "-format");
            }
        }

        return new Configuration(inputPath, outputPath, parallelism, batchSize, writer, format);
    }

    private static <T> T readOrFail(Iterator<T> it, String flagName) {

        if (!it.hasNext()) {
            throw new RuntimeException(String.format("No value provided for %s flag", flagName));
        }
        return it.next();
    }
}
