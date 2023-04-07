package dev.polluxus.scryfall_projects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        final ObjectMapper mapper = new ObjectMapper()
                .registerModule(new Jdk8Module())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        final List<ScryfallCard> cards;
        try {
            final byte[] in = Files.readAllBytes(Path.of("src/main/resources/card_data/first-1000-cards.json"));
            cards = mapper.readValue(in, new TypeReference<>() {});
            System.out.println(cards.get(0));
        } catch (IOException e) {
            System.out.println("FUCK!");
            System.out.println(e);
            System.exit(1);
        }



    }
}
