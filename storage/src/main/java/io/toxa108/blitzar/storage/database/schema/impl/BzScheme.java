package io.toxa108.blitzar.storage.database.schema.impl;

import io.toxa108.blitzar.storage.database.schema.Field;
import io.toxa108.blitzar.storage.database.schema.Index;
import io.toxa108.blitzar.storage.database.schema.Scheme;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BzScheme implements Scheme {
    private final Set<Field> fields;
    private final Set<Index> indexes;

    public BzScheme(final Set<Field> fields, final Set<Index> indexes) {
        if (indexes.stream().filter(it -> it.type() == IndexType.PRIMARY).count() > 1) {
            throw new IllegalArgumentException("Table must include only one PRIMARY index");
        }

        this.fields = fields;
        this.indexes = indexes;
    }

    @Override
    public Set<Field> fields() {
        return fields;
    }

    @Override
    public Set<Index> indexes() {
        return indexes;
    }

    @Override
    public Index primaryIndex() {
        return indexes().stream()
                .filter(it -> it.type() == IndexType.PRIMARY)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Table has to contain ONE primary index"));
    }

    @Override
    public Field primaryIndexField() {
        return primaryIndex()
                .fields()
                .stream()
                .map(it -> fields().stream()
                        .filter(it2 -> it2.name().equals(it))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException(
                                "Table has to contain ONE primary index"))
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Table has to contain ONE primary index"));
    }

    @Override
    public int primaryIndexSize() {
        return primaryIndex()
                .fields()
                .stream()
                .map(it -> fields().stream()
                        .filter(it2 -> it2.name().equals(it))
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException(
                                "Table has to contain ONE primary index"))
                        .diskSize()
                )
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public boolean isVariable() {
        Set<String> indexFields = primaryIndex().fields();

        return this.fields().stream()
                .filter(it -> indexFields.contains(it.name()))
                .anyMatch(Field::isVariable);
    }

    @Override
    public int recordSize() {
        return this.fields.stream()
                .map(Field::diskSize)
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public int dataSize() {
        return this.fields.stream()
                .filter(it -> !primaryIndex().fields().contains(it.name()))
                .map(Field::diskSize)
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public Set<Field> dataFields() {
        return this.fields.stream()
                .filter(it -> !primaryIndex().fields().contains(it.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean containIndex(final String indexName) {
        return this.indexes.stream().anyMatch(it -> it.fields().size() == 1 && it.fields().contains(indexName));
    }

    @Override
    public Field fieldByName(final String fieldName) {
        return this.fields.stream()
                .filter(it -> it.name().equals(fieldName))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("Field " + fieldName + " isn't found"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BzScheme scheme = (BzScheme) o;
        return Objects.equals(fields, scheme.fields) &&
                Objects.equals(indexes, scheme.indexes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fields, indexes);
    }
}
