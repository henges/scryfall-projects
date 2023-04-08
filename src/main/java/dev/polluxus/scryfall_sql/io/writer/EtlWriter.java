package dev.polluxus.scryfall_sql.io.writer;

import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.MagicSet;

import java.util.Collection;

public interface EtlWriter {

    void write(Collection<MagicSet> sets, Collection<Card> cards, Collection<CardEdition> editions);

    void commit();
}
