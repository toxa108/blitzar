package io.toxa108.blitzar.storage.io.impl;

import io.toxa108.blitzar.storage.database.context.DatabaseConfiguration;
import io.toxa108.blitzar.storage.database.context.impl.BzDatabaseConfiguration;
import io.toxa108.blitzar.storage.database.schema.Database;
import io.toxa108.blitzar.storage.database.schema.Scheme;
import io.toxa108.blitzar.storage.database.schema.Table;
import io.toxa108.blitzar.storage.database.schema.impl.*;
import io.toxa108.blitzar.storage.io.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BzFileManagerTest {
    DatabaseConfiguration databaseConfiguration = new BzDatabaseConfiguration(16);

    @BeforeEach
    public void before() throws IOException {
        FileManager fileManager = new TestBzFileManager("/tmp/blitzar", databaseConfiguration);
        fileManager.clear();
    }

    @Test
    public void save_table_metadata_to_the_table_file_when_success() throws IOException {
        FileManager fileManager = new TestBzFileManager("/tmp/blitzar", databaseConfiguration);
        Database database = fileManager.initializeDatabase("test");
        Scheme scheme = new BzScheme(
                Set.of(new BzField("id", FieldType.LONG, Nullable.NOT_NULL, Unique.UNIQUE, new byte[0]),
                        new BzField("name", FieldType.VARCHAR, Nullable.NOT_NULL, Unique.NOT_UNIQUE, new byte[10])
                ),
                Set.of(
                        new BzIndex(Set.of("id"), IndexType.PRIMARY),
                        new BzIndex(Set.of("id", "name"), IndexType.SECONDARY)
                )
        );

        Table table = database.createTable("table", scheme);
        Table loadedTable = fileManager.loadTable(database.name(), table.name());
        assertEquals(scheme, loadedTable.scheme());
    }
}