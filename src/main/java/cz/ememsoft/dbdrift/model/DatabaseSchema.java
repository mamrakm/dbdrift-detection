package cz.ememsoft.dbdrift.model;

import lombok.NonNull;
import java.util.Map;
import java.util.Set;

/**
 * Reprezentuje schému databázy s tabuľkami a ich stĺpcami.
 */
public record DatabaseSchema(@NonNull Map<TableName, Set<ColumnName>> tables) {}