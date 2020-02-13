package io.toxa108.blitzar.storage.database.manager.user;

import java.util.HashMap;
import java.util.Map;

public class BzUserManager implements UserManager {
    private final Map<String, User> users;

    public BzUserManager() {
        this.users = new HashMap<>();
    }

    @Override
    public User createUser(final String login, final String password) {
        return users.put(login, new BzUser(login, password));
    }

    @Override
    public User authorize(String login, String password) throws AccessDeniedException {
        User user = users.get(login);
        if (user.password().equals(password)) {
            return user;
        }
        throw new AccessDeniedException();
    }

    @Override
    public void clear() {
        users.clear();
    }
}