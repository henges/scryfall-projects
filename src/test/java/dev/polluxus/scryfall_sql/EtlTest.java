package dev.polluxus.scryfall_sql;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.etl.Etl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

//@Timeout(value = 15, unit = TimeUnit.SECONDS)
public class EtlTest {

    @Test
    public void testSql_tmpFile_parallel50() {

        Etl.run(new Configuration(
                null,
                "output/test.sql",
                "50",
                "1000",
                "TMP_FILE",
                "SQL"
        ));
    }

    @Test
    public void testJson_tmpFile_parallel50() {

        Etl.run(new Configuration(
                null,
                "output/test.json",
                "50",
                "1000",
                "TMP_FILE",
                "JSON"
        ));
    }

    @Test
    public void testSql_inMemory_parallel50() {

        Etl.run(new Configuration(
                null,
                "output/test.sql",
                "50",
                "1000",
                "IN_MEMORY",
                "SQL"
        ));
    }

    @Test
    public void testJson_inMemory_parallel50() {

        Etl.run(new Configuration(
                null,
                "output/test.json",
                "50",
                "1000",
                "IN_MEMORY",
                "JSON"
        ));
    }
}
