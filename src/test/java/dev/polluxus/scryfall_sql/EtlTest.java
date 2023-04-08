package dev.polluxus.scryfall_sql;

import dev.polluxus.scryfall_sql.etl.Configuration;
import dev.polluxus.scryfall_sql.etl.Etl;
import org.junit.jupiter.api.Test;

public class EtlTest {

    @Test
    public void test() {

        Etl.run(new Configuration(null, "output/test.sql", "50", "1000", "TMP_FILE"));
    }
}
