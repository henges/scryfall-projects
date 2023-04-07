package dev.polluxus.scryfall_projects.scryfall.converter;

import dev.polluxus.scryfall_projects.model.MagicSet;
import dev.polluxus.scryfall_projects.scryfall.model.ScryfallCard;

public class ScryfallCardMagicSetConverter implements Converter<ScryfallCard, MagicSet> {

    @Override
    public MagicSet convert(ScryfallCard source) {

        return new MagicSet(source.set(), source.setName(), source.releasedAt().toInstant());
    }
}
