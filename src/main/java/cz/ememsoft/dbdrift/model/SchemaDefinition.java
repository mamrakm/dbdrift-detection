package cz.ememsoft.dbdrift.model;

import lombok.NonNull;

import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Kanonická, utriedená reprezentácia databázovej schémy.
 */
public record SchemaDefinition(@NonNull SortedMap<TableName, SortedSet<ColumnName>> tables) {}