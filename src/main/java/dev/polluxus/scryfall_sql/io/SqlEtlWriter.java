package dev.polluxus.scryfall_sql.io;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.model.Card;
import dev.polluxus.scryfall_sql.model.Card.CardEdition;
import dev.polluxus.scryfall_sql.model.Card.CardFace;
import dev.polluxus.scryfall_sql.model.MagicSet;
import dev.polluxus.scryfall_sql.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class SqlEtlWriter implements EtlWriter {

    private static final Logger log = LoggerFactory.getLogger(SqlEtlWriter.class);

    private final Writer outputWriter;

    private final Writer setWriter;
    private final Writer cardWriter;
    private final Writer editionWriter;

    private final Path setPath;
    private final Path cardPath;
    private final Path editionPath;

    public SqlEtlWriter(Configuration config) {
        this.outputWriter = IO.openWriter(config);
        var sets = IO.writerForTempFile("sets");
        var cards = IO.writerForTempFile("cards");
        var editions = IO.writerForTempFile("editions");
        setWriter = sets.getLeft();
        cardWriter = cards.getLeft();
        editionWriter = editions.getLeft();
        setPath = sets.getRight();
        cardPath = cards.getRight();
        editionPath = editions.getRight();
    }

    @Override
    public void start() {
        // Noop
    }

    @Override
    public void end() {

        try {

            log.info("Beginning final write");

            // Close the writers for the temp files
            setWriter.flush();
            cardWriter.flush();
            editionWriter.flush();

            setWriter.close();
            cardWriter.close();
            editionWriter.close();

            // Open readers for each temp file
            Reader setReader = IO.openReader(setPath);
            Reader cardReader = IO.openReader(cardPath);
            Reader editionReader = IO.openReader(editionPath);

            // Write all data to the output
            writeString(outputWriter, "BEGIN TRANSACTION;\n");

            setReader.transferTo(outputWriter);
            cardReader.transferTo(outputWriter);
            // Write editions last since they have set/card FKs
            editionReader.transferTo(outputWriter);

            writeString(outputWriter, "END TRANSACTION;\n");

            setReader.close();
            cardReader.close();
            editionReader.close();

            Files.delete(setPath);
            Files.delete(cardPath);
            Files.delete(editionPath);

            outputWriter.flush();
            outputWriter.close();

            log.info("Results written successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void write(Collection<MagicSet> sets, Collection<Card> cards, Collection<CardEdition> editions) {

        sets.forEach(s -> writeString(setWriter, getSetUpsertSql(s)));
        cards.forEach(s -> writeString(cardWriter, getCardUpsertSql(s)));
        editions.forEach(s -> writeString(editionWriter, getEditionUpsertSql(s)));
    }

    private static final String SET_UPSERT_STATEMENT = """
            INSERT INTO scryfall.set(code, name, release_date) VALUES ('$1', E'$2', '$3') ON CONFLICT DO NOTHING;
            """.trim();

    private String getSetUpsertSql(MagicSet set) {

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

    private String getCardUpsertSql(Card card) {

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

    private String getEditionUpsertSql(CardEdition ed) {

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

    private void writeString(Writer writer, String string) {

        try {
            writer.write(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
