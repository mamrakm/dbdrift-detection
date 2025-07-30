package cz.ememsoft.dbdrift.model;

/**
 * Zapečatené (sealed) rozhranie definujúce spoločný kontrakt pre všetky typy schém v aplikácii.
 */
public sealed interface Schema permits DatabaseSchema, JpaSchema {
    SchemaDefinition definition();
}