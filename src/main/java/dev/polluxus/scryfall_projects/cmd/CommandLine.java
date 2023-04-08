package dev.polluxus.scryfall_projects.cmd;

import dev.polluxus.scryfall_projects.Etl;

import java.util.Arrays;
import java.util.Iterator;

public class CommandLine {

    public static void main(String[] args) {

        System.out.println(Arrays.toString(args));

        Configuration config = fromArgs(args);

        Etl.run(config);
    }

    private static Configuration fromArgs(String[] args) {

        Iterator<String> it = Arrays.asList(args).iterator();
        String inputPath = null,
                outputPath = null,
                parallelism = "1";

        while (it.hasNext()) {
            final String s = it.next();
            switch (s) {
                case "-in" -> inputPath = readOrFail(it, "-in");
                case "-out" -> outputPath = readOrFail(it, "-out");
                case "-p" -> parallelism = readOrFail(it, "-p");
                default -> {}
            }
        }

        return new Configuration(inputPath, outputPath, parallelism);
    }

    private static <T> T readOrFail(Iterator<T> it, String flagName) {

        if (!it.hasNext()) {
            throw new RuntimeException(String.format("No value provided for %s flag", flagName));
        }
        return it.next();
    }
}
