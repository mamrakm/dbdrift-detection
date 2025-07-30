package cz.ememsoft.dbdrift.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NonNull;

/**
 * Typovo bezpečná a nemenná reprezentácia názvu databázovej tabuľky.
 */
public record TableName(@JsonValue @NonNull String value) implements Comparable<TableName> {
    @Override public int compareTo(TableName other) { return this.value.compareTo(other.value()); }
}