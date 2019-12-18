package io.toxa108.blitzar.storage.database.schema.impl;

import io.toxa108.blitzar.storage.database.schema.Table;
import io.toxa108.blitzar.storage.io.FileManager;
import io.toxa108.blitzar.storage.io.impl.TestFileManagerImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class TableImplTest {
    FileManager fileManager = new TestFileManagerImpl("/tmp/blitzar");

    @Before
    public void before() {
        fileManager.clear();
    }

    @Test
    public void create_table_when_success() {
        final Table table = new TableImpl("table", new SchemeImpl(Set.of(), Set.of()), fileManager);
        assertNotNull(table);
    }
}