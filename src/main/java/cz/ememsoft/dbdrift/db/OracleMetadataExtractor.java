package cz.ememsoft.dbdrift.db;


import cz.ememsoft.dbdrift.config.DatabaseConfig;
import cz.ememsoft.dbdrift.exception.ApplicationExceptions;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.SchemaDefinition;
import cz.ememsoft.dbdrift.model.TableName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Extrahuje metadáta schémy (tabuľky a stĺpce) z Oracle databázy.
 */
@Slf4j
public class OracleMetadataExtractor {
    private final DatabaseConfig config;
    private static final String METADATA_QUERY = """
        SELECT c.table_name, c.column_name FROM ALL_TAB_COLUMNS c
        JOIN ALL_TABLES t ON c.owner = t.owner AND c.table_name = t.table_name
        WHERE c.owner = ? ORDER BY c.table_name, c.column_name""";

    public OracleMetadataExtractor(@NonNull DatabaseConfig config) { this.config = config; }

    public DatabaseSchema fetchSchema() {
        log.info("Získavam metadáta schémy z databázy pre vlastníka: '{}'", config.schema());
        SortedMap<TableName, SortedSet<ColumnName>> tables = new TreeMap<>();
        int columnCount = 0;
        try (Connection conn = DatabaseConnectionFactory.createConnection(config);
             PreparedStatement stmt = conn.prepareStatement(METADATA_QUERY)) {
            stmt.setString(1, config.schema());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TableName tableName = new TableName(rs.getString("table_name").toUpperCase());
                    ColumnName columnName = new ColumnName(rs.getString("column_name").toUpperCase());
                    tables.computeIfAbsent(tableName, k -> new TreeSet<>()).add(columnName);
                    log.trace("Nájdený stĺpec v DB: {}.{}", tableName.value(), columnName.value());
                    columnCount++;
                }
            }
        } catch (SQLException e) {
            log.error("SQL chyba pri extrakcii metadát z Oracle.", e);
            throw new ApplicationExceptions.MetadataExtractionException("Nepodarilo sa extrahovať metadáta schémy Oracle.", e);
        }
        log.info("Úspešne extrahované metadáta pre {} tabuliek a {} stĺpcov.", tables.size(), columnCount);
        return new DatabaseSchema(new SchemaDefinition(tables));
    }
}