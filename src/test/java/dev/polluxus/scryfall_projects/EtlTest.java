package dev.polluxus.scryfall_projects;

import dev.polluxus.scryfall_projects.cmd.Configuration;
import org.junit.jupiter.api.Test;

public class EtlTest {

    @Test
    public void test() {

        Etl.run(new Configuration(null, "output/test-parallel4.sql", "1"));
    }
}
