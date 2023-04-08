package dev.polluxus.scryfall_sql.scryfall.converter;

public interface Converter<S, T> {

    T convert(S source);
}
