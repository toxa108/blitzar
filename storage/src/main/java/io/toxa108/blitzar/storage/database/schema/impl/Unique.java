package io.toxa108.blitzar.storage.database.schema.impl;

import java.util.NoSuchElementException;

public enum Unique {
    /**
     * unique
     */
    UNIQUE((short) 1),
    /**
     * Not unique
     */
    NOT_UNIQUE((short) 2);

    private final short id;

    private Unique(final short id) {
        this.id = id;
    }

    public short id() {
        return id;
    }

    public static Unique fromId(final short id) {
        for (Unique e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        throw new NoSuchElementException("Such unique type doesn't exist");
    }

    public int size() {
        return Short.BYTES;
    }
}
