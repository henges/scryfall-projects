package dev.polluxus.scryfall_sql.io.format;

import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.Card.CardFace;
import dev.polluxus.scryfall_sql.model.MagicSet;
import dev.polluxus.scryfall_sql.util.StringUtils;

import java.util.Optional;

public class SqlFormat implements EtlFormat {

    @Override
    public Optional<String> start() {
        return Optional.of("BEGIN TRANSACTION;\n");
    }

    @Override
    public Optional<String> end() {
        return Optional.of("END TRANSACTION;\n");
    }

    private static final String SET_UPSERT_STATEMENT = """
            INSERT INTO scryfall.set(code, name, release_date) VALUES ('$1', E'$2', '$3') ON CONFLICT DO NOTHING;
            """.trim();

    @Override
    public String set(MagicSet set) {

        final String sql = SET_UPSERT_STATEMENT
                .replace("$1", set.code())
                .replace("$2", StringUtils.escapeQuotes(set.name()))
                .replace("$3", set.releaseDate().toString());

        return sql + "\n";
    }

    private static final String CARD_UPSERT_STATEMENT = """
            INSERT INTO scryfall.card(id, name, formats, colour_identity, keywords)
            VALUES (
                '$1',
                E'$2',
                ARRAY[$3]::scryfall.format[],
                ARRAY[$4]::scryfall.colour[],
                ARRAY[$5]
            ) ON CONFLICT DO NOTHING;
            """.trim().replaceAll("([\s\n])+", " ");

    private static final String CARD_FACE_UPSERT_STATEMENT = """
            INSERT INTO scryfall.card_face(
                card_id, name, mana_value, mana_cost, types,
                subtypes, oracle_text, power, toughness, loyalty,
                colours
            )
            VALUES(
                '$01', E'$02', $03, ARRAY[$04], ARRAY[$05],
                ARRAY[$06], E'$07', $08, $09, $10,
                ARRAY[$11]::scryfall.colour[]
            ) ON CONFLICT DO NOTHING;
            """.trim().replaceAll("([\s\n])+", " ");

    @Override
    public String card(Card card) {

        final StringBuilder sb = new StringBuilder();

        sb.append(CARD_UPSERT_STATEMENT
                .replace("$1", card.id().toString())
                .replace("$2", StringUtils.escapeQuotes(card.name()))
                .replace("$3", StringUtils.delimitedString(card.formats()))
                .replace("$4", StringUtils.delimitedString(card.colourIdentity()))
                .replace("$5", StringUtils.delimitedString(card.keywords(), true)));

        for (CardFace cf : card.faces()) {

            sb.append("\n");

            sb.append(CARD_FACE_UPSERT_STATEMENT
                    .replace("$01", cf.cardId().toString())
                    .replace("$02", StringUtils.escapeQuotes(cf.name()))
                    .replace("$03", String.format("%d", cf.manaValue()))
                    .replace("$04", StringUtils.delimitedString(cf.manaCost()))
                    .replace("$05", StringUtils.delimitedString(cf.types(), true))
                    .replace("$06", StringUtils.delimitedString(cf.subtypes(), true))
                    .replace("$07", StringUtils.escapeQuotes(cf.oracleText()))
                    .replace("$08", StringUtils.quotedStringOrNullLiteral(cf.power()))
                    .replace("$09", StringUtils.quotedStringOrNullLiteral(cf.toughness()))
                    .replace("$10", StringUtils.quotedStringOrNullLiteral(cf.loyalty()))
                    .replace("$11", StringUtils.delimitedString(cf.colours())));
        }

        return sb + "\n";
    }

    private static final String EDITION_UPSERT_STATEMENT = """
            INSERT INTO scryfall.card_edition(id, card_id, set_code, collector_number, rarity, is_reprint, scryfall_url, games)
            VALUES ('$1', '$2', '$3', '$4', '$5'::scryfall.rarity, $6, '$7', ARRAY[$8]::scryfall.game[])
            ON CONFLICT DO NOTHING;
            """.trim().replaceAll("([\s\n])+", " ");

    @Override
    public String edition(CardEdition ed) {

        final String sql = EDITION_UPSERT_STATEMENT
                .replace("$1", ed.id().toString())
                .replace("$2", ed.cardId().toString())
                .replace("$3", ed.setCode())
                .replace("$4", ed.collectorNumber())
                .replace("$5", ed.rarity().toString())
                .replace("$6", String.format("%b", ed.isReprint()))
                .replace("$7", ed.scryfallUrl())
                .replace("$8", StringUtils.delimitedString(ed.games()));

        return sql + "\n";
    }
}
