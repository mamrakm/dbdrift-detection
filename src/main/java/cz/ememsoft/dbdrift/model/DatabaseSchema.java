package cz.ememsoft.dbdrift.model;

import lombok.NonNull;
/**
 * Finálna trieda pre schému z databázy, implementujúca zapečatené rozhranie Schema.
 */
public final record DatabaseSchema(@NonNull SchemaDefinition definition) implements Schema {}