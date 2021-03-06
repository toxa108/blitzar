package io.toxa108.blitzar.storage.database.schema.impl;

import io.toxa108.blitzar.storage.database.context.DatabaseConfiguration;
import io.toxa108.blitzar.storage.database.context.impl.BzDatabaseConfiguration;
import io.toxa108.blitzar.storage.database.manager.row.BzRows;
import io.toxa108.blitzar.storage.database.schema.Scheme;
import io.toxa108.blitzar.storage.database.schema.Table;
import io.toxa108.blitzar.storage.io.FileManager;
import io.toxa108.blitzar.storage.io.impl.TestBzFileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TableImplTest {
    DatabaseConfiguration databaseConfiguration = new BzDatabaseConfiguration(16);
    FileManager fileManager = new TestBzFileManager("/tmp/blitzar", databaseConfiguration);

    public TableImplTest() throws IOException {
    }

    @BeforeEach
    public void before() {
        fileManager.clear();
    }

    @Test
    public void create_table_when_success() throws IOException {
        File file = Files.createTempFile("q1", "12").toFile();
        file.deleteOnExit();

        Scheme scheme = new BzScheme(Set.of(
                new BzField("id", FieldType.LONG, Nullable.NOT_NULL, Unique.UNIQUE, new byte[Long.BYTES])),
                Set.of(new BzIndex(Set.of("id"), IndexType.PRIMARY)));

        final Table table = new BzTable(
                "table",
                scheme,
                new BzRows(file, scheme, new BzDatabaseConfiguration(1))
        );
        assertNotNull(table);
    }
}