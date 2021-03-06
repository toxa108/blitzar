package io.toxa108.blitzar.storage.database.schema.impl;

import java.util.NoSuchElementException;

public enum Nullable {
    /**
     * Nullable
     */
    NULL((short) 1),
    /**
     * Not nullable
     */
    NOT_NULL((short) 2);

    private final short id;

    Nullable(final short id) {
        this.id = id;
    }

    public short id() {
        return id;
    }

    public static Nullable fromId(final short id) {
        for (Nullable e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        throw new NoSuchElementException("Such nullable type doesn't exist");
    }

    public int size() {
        return Short.BYTES;
    }
}
