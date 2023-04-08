package dev.polluxus.scryfall_projects.io;

import dev.polluxus.scryfall_projects.model.Card;
import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.MagicSet;

import java.util.Collection;

public interface EtlWriter {

    void start();

    void write(Collection<MagicSet> sets, Collection<Card> cards, Collection<CardEdition> editions);

    void end();
}
