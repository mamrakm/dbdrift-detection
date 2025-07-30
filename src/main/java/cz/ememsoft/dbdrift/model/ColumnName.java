package cz.ememsoft.dbdrift.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.NonNull;

/**
 * Typovo bezpečná a nemenná reprezentácia názvu databázového stĺpca.
 */
public record ColumnName(@JsonValue @NonNull String value) implements Comparable<ColumnName> {
    @Override public int compareTo(ColumnName other) { return this.value.compareTo(other.value()); }
}