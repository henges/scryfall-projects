package dev.polluxus.scryfall_sql.io.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.MagicSet;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonFormat implements EtlFormat {

    private final AtomicBoolean hasPrevious = new AtomicBoolean(false);

    private final ObjectMapper mapper;

    public JsonFormat(Configuration configuration) {
        this.mapper = configuration.mapper();
    }

    @Override
    public Optional<String> start() {
        return Optional.of("[");
    }

    @Override
    public Optional<String> end() {
        return Optional.of("null]");
    }

    @Override
    public String card(Card card) {
        return writeValueAsString(card);
    }

    @Override
    public String set(MagicSet set) {
        return writeValueAsString(set);
    }

    @Override
    public String edition(CardEdition edition) {
        return writeValueAsString(edition);
    }

    private static final String JSON_TEMPLATE = "{\"type\": \":type\", \"data\": :data},\n";

    private String writeValueAsString(Object obj) {

        try {

            return JSON_TEMPLATE
                    .replace(":type", obj.getClass().getSimpleName())
                    .replace(":data", mapper.writeValueAsString(obj));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
