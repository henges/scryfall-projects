package dev.polluxus.scryfall_sql.io.format;

import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.MagicSet;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Optional;

public interface EtlFormat {

    Optional<String> start();

    Optional<String> end();

    String card(Card card);

    default Collection<String> cards(Collection<Card> cards) {

        return cards.stream()
                .map(this::card)
                .toList();
    }

    String set(MagicSet set);

    default Collection<String> sets(Collection<MagicSet> sets) {

        return sets.stream()
                .map(this::set)
                .toList();
    }

    String edition(CardEdition edition);

    default Collection<String> editions(Collection<CardEdition> editions) {

        return editions.stream()
                .map(this::edition)
                .toList();
    }
}
