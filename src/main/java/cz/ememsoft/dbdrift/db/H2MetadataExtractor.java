package cz.ememsoft.dbdrift.db;

import cz.ememsoft.dbdrift.exception.ApplicationExceptions;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.TableName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Extrahuje metadáta schémy (tabuľky a stĺpce) z H2 databázy.
 * Používa sa hlavne pre testovanie.
 */
@Slf4j
public class H2MetadataExtractor {
    private static final String METADATA_QUERY = """
        SELECT TABLE_NAME, COLUMN_NAME 
        FROM INFORMATION_SCHEMA.COLUMNS 
        WHERE TABLE_SCHEMA = ? 
        ORDER BY TABLE_NAME, COLUMN_NAME""";

    public DatabaseSchema extractDatabaseSchema(@NonNull Connection connection, @NonNull String schemaName) {
        log.info("Získavam metadáta schémy z H2 databázy pre schému: '{}'", schemaName);
        Map<TableName, Set<ColumnName>> tables = new LinkedHashMap<>();
        int columnCount = 0;
        
        try (PreparedStatement stmt = connection.prepareStatement(METADATA_QUERY)) {
            stmt.setString(1, schemaName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TableName tableName = new TableName(rs.getString("TABLE_NAME").toUpperCase());
                    ColumnName columnName = new ColumnName(rs.getString("COLUMN_NAME").toUpperCase());
                    tables.computeIfAbsent(tableName, k -> new LinkedHashSet<>()).add(columnName);
                    log.trace("Nájdený stĺpec v H2 DB: {}.{}", tableName.value(), columnName.value());
                    columnCount++;
                }
            }
        } catch (SQLException e) {
            log.error("SQL chyba pri extrakcii metadát z H2.", e);
            throw new ApplicationExceptions.MetadataExtractionException("Nepodarilo sa extrahovať metadáta schémy H2.", e);
        }
        
        log.info("Úspešne extrahované metadáta pre {} tabuliek a {} stĺpcov z H2.", tables.size(), columnCount);
        return new DatabaseSchema(tables);
    }
}