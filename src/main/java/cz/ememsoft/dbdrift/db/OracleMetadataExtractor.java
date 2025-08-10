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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extrahuje metadáta schémy (tabuľky a stĺpce) z Oracle databázy.
 */
@Slf4j
public class OracleMetadataExtractor {
    private static final String METADATA_QUERY = """
        SELECT c.table_name, c.column_name FROM ALL_TAB_COLUMNS c
        JOIN ALL_TABLES t ON c.owner = t.owner AND c.table_name = t.table_name
        WHERE c.owner = ? ORDER BY c.table_name, c.column_name""";

    public DatabaseSchema extractDatabaseSchema(@NonNull Connection connection, @NonNull String schemaName) {
        log.info("Získavam metadáta schémy z databázy pre vlastníka: '{}'", schemaName);
        Map<TableName, Set<ColumnName>> tables = new LinkedHashMap<>();
        int columnCount = 0;
        
        try (PreparedStatement stmt = connection.prepareStatement(METADATA_QUERY)) {
            stmt.setString(1, schemaName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TableName tableName = new TableName(rs.getString("table_name").toUpperCase());
                    ColumnName columnName = new ColumnName(rs.getString("column_name").toUpperCase());
                    tables.computeIfAbsent(tableName, k -> new LinkedHashSet<>()).add(columnName);
                    log.trace("Nájdený stĺpec v DB: {}.{}", tableName.value(), columnName.value());
                    columnCount++;
                }
            }
        } catch (SQLException e) {
            log.error("SQL chyba pri extrakcii metadát z Oracle.", e);
            throw new ApplicationExceptions.MetadataExtractionException("Nepodarilo sa extrahovať metadáta schémy Oracle.", e);
        }
        log.info("Úspešne extrahované metadáta pre {} tabuliek a {} stĺpcov.", tables.size(), columnCount);
        return new DatabaseSchema(tables);
    }
}