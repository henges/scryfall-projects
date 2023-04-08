package dev.polluxus.scryfall_projects;

import dev.polluxus.scryfall_projects.cmd.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class IO {

    public static Pair<Reader, Writer> openFiles(Configuration config) {

        try {
            final var fileReader = Files.newBufferedReader(Path.of(config.inputPath()));
            final FileWriter writer = new FileWriter(config.outputPath());

            return Pair.of(fileReader, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeFiles(Pair<Reader, Writer> rw) {

        if (rw == null) {
            return;
        }

        Reader r = rw.getLeft();
        Writer w = rw.getRight();

        if (r == null || w == null) {
            return;
        }

        try {
            r.close();
        } catch (IOException e) {
            System.out.println("Error closing reader: " + e);
        }

        try {
            w.flush();
            w.close();
        }  catch (IOException e) {
            throw new RuntimeException("Error closing writer!", e);
        }
    }
}
