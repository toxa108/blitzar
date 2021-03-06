package io.toxa108.blitzar.storage.database.context.impl;

import io.toxa108.blitzar.storage.database.context.DatabaseContext;
import io.toxa108.blitzar.storage.database.schema.Database;
import io.toxa108.blitzar.storage.database.schema.impl.BzDatabase;
import io.toxa108.blitzar.storage.io.FileManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BzDatabaseContext implements DatabaseContext {
    private final Set<Database> databases;
    private final FileManager fileManager;

    public BzDatabaseContext(final FileManager fileManager) throws IOException {
        this.fileManager = fileManager;

        Set<Database> set = new HashSet<>();
        for (String it : fileManager.databases()) {
            BzDatabase database = new BzDatabase(it, fileManager);
            set.add(database);
        }
        this.databases = set;
    }

    @Override
    public Optional<Database> findByName(final String name) {
        return databases.stream()
                .filter(it -> it.name().equals(name))
                .findAny();
    }

    @Override
    public Database createDatabase(final String name) throws IOException {
        Optional<Database> databaseOptional = databases.stream()
                .filter(it -> it.name().equals(name))
                .findFirst();
        if (databaseOptional.isPresent()) {
            return databaseOptional.get();
        } else {
            Database database = fileManager.initializeDatabase(name);
            databases.add(database);
            return database;
        }
    }

    @Override
    public Set<Database> databases() {
        return databases;
    }
}
