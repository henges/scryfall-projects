package dev.polluxus.scryfall_projects.model;

import java.time.Instant;

public record MagicSet(
        String code,
        String name,
        Instant releaseDate
) {
}
