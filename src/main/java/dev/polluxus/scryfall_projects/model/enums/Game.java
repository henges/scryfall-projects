package dev.polluxus.scryfall_projects.model.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public enum Game {

    ARENA("arena"),
    PAPER("paper"),
    MTGO("mtgo");

    final String gameName;

    private static final Map<String, Game> STRING_TO_GAME_MAP = EnumSet.allOf(Game.class)
            .stream()
            .collect(Collectors.toMap(
                    f -> f.gameName,
                    f -> f
            ));

    Game(String gameName) {
        this.gameName = gameName;
    }

    public static Game get(String name) {
        return STRING_TO_GAME_MAP.get(name);
    }
}
