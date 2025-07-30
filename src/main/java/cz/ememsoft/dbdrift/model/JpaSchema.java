package cz.ememsoft.dbdrift.model;

import lombok.NonNull;
/**
 * Finálna trieda pre schému z JPA, implementujúca zapečatené rozhranie Schema.
 */
public final record JpaSchema(@NonNull SchemaDefinition definition) implements Schema {}