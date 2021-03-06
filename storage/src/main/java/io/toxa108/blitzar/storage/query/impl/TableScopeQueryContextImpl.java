package io.toxa108.blitzar.storage.query.impl;

import io.toxa108.blitzar.storage.database.schema.Database;
import io.toxa108.blitzar.storage.database.schema.Table;
import io.toxa108.blitzar.storage.query.QueryContext;

public class TableScopeQueryContextImpl implements QueryContext {
    private final Database database;
    private final Table table;

    public TableScopeQueryContextImpl(final Database database, final Table table) {
        this.database = database;
        this.table = table;
    }

    @Override
    public Database database() {
        return database;
    }

    @Override
    public Table table() {
        return table;
    }
}
