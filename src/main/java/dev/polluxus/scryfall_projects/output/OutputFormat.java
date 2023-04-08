package dev.polluxus.scryfall_projects.output;

import dev.polluxus.scryfall_projects.model.Card;
import dev.polluxus.scryfall_projects.model.Card.CardEdition;
import dev.polluxus.scryfall_projects.model.MagicSet;

import java.util.Collection;

public interface OutputFormat {

    void output(Collection<MagicSet> sets, Collection<Card> cards, Collection<CardEdition> editions);
}
