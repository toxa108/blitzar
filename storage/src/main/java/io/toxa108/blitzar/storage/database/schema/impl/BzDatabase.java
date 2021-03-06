package io.toxa108.blitzar.storage.database.schema.impl;

import io.toxa108.blitzar.storage.database.schema.Database;
import io.toxa108.blitzar.storage.database.schema.Scheme;
import io.toxa108.blitzar.storage.database.schema.Table;
import io.toxa108.blitzar.storage.io.FileManager;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BzDatabase implements Database {
    private final List<Table> tables;
    private final String name;
    private final FileManager fileManager;
    private final State state;

    public BzDatabase(final String name,
                      final List<Table> tables,
                      final FileManager fileManager) {
        this.tables = tables;
        this.name = name;
        this.fileManager = fileManager;
        this.state = State.EXISTS;
    }

    public BzDatabase(final String name,
                      final FileManager fileManager) throws IOException {
        this.name = name;
        this.fileManager = fileManager;
        this.tables = fileManager.loadTables(name);
        this.state = State.EXISTS;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<Table> findTableByName(final String name) {
        return tables.stream()
                .filter(it -> it.name().equalsIgnoreCase(name))
                .findAny();
    }

    @Override
    public Table createTable(final String name,
                             final Scheme scheme) throws IOException {
        if (this.state == State.REMOVED) {
            throw new IllegalStateException("Database removed, can't create table.");
        }
        Table table = fileManager.initializeTable(this.name, name, scheme);
        tables.add(table);
        return table;
    }

    @Override
    public List<Table> tables() {
        return tables;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BzDatabase database = (BzDatabase) o;
        return Objects.equals(name, database.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
