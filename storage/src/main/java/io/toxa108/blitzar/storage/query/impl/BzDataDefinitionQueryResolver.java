package io.toxa108.blitzar.storage.query.impl;

import io.toxa108.blitzar.storage.database.context.DatabaseContext;
import io.toxa108.blitzar.storage.database.schema.Database;
import io.toxa108.blitzar.storage.database.schema.impl.BzScheme;
import io.toxa108.blitzar.storage.query.DataDefinitionQueryResolver;
import io.toxa108.blitzar.storage.query.ResultQuery;

import java.io.IOException;
import java.util.Optional;

public class BzDataDefinitionQueryResolver implements DataDefinitionQueryResolver {
    private final DatabaseContext databaseContext;

    public BzDataDefinitionQueryResolver(final DatabaseContext databaseContext) {
        this.databaseContext = databaseContext;
    }

    @Override
    public ResultQuery createDatabase(final DataDefinitionQuery query) {
        try {
            databaseContext.createDatabase(query.databaseName());
            return new EmptySuccessResultQuery();
        } catch (IOException e) {
            e.printStackTrace();
            return new ErrorResultQuery();
        }
    }

    @Override
    public ResultQuery createTable(final DataDefinitionQuery query) {
        Optional<Database> databaseOptional = databaseContext.findByName(query.databaseName());
        if (databaseOptional.isEmpty()) {
            return new ErrorResultQuery();
        } else {
            Database database = databaseOptional.get();
            try {
                database.createTable(query.tableName(), new BzScheme(query.fields(), query.getIndexes()));
                return new EmptySuccessResultQuery();
            } catch (IOException e) {
                e.printStackTrace();
                return new ErrorResultQuery();
            }
        }
    }

    @Override
    public ResultQuery createIndex(final DataDefinitionQuery query) {
        return new EmptySuccessResultQuery();
    }

    @Override
    public ResultQuery dropDatabase(DataDefinitionQuery query) {
        return new EmptySuccessResultQuery();
    }

    @Override
    public ResultQuery dropTable(DataDefinitionQuery query) {
        return new EmptySuccessResultQuery();
    }

    @Override
    public ResultQuery dropIndex(DataDefinitionQuery query) {
        return new EmptySuccessResultQuery();
    }
}
