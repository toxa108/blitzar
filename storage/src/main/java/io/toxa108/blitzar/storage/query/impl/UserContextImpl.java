package io.toxa108.blitzar.storage.query.impl;

import io.toxa108.blitzar.storage.database.manager.user.User;
import io.toxa108.blitzar.storage.query.UserContext;

public class UserContextImpl implements UserContext {
    private final String databaseName;
    private final User user;

    public UserContextImpl(final User user) {
        this.databaseName = "";
        this.user = user;
    }

    public UserContextImpl(final String databaseName,
                           final User user) {
        this.databaseName = databaseName;
        this.user = user;
    }

    @Override
    public String databaseName() {
        return databaseName;
    }

    @Override
    public User user() {
        return user;
    }
}
