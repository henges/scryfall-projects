package dev.polluxus.scryfall_projects.scryfall.converter;

public interface Converter<S, T> {

    T convert(S source);
}
