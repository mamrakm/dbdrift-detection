package cz.ememsoft.dbdrift.service;

import cz.ememsoft.dbdrift.db.DatabaseConnectionFactory;
import cz.ememsoft.dbdrift.db.OracleMetadataExtractor;
import cz.ememsoft.dbdrift.jpa.EntityDiscovery;
import cz.ememsoft.dbdrift.jpa.JpaEntityAnalyzer;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.TableName;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SchemaComparisonService {
    
    private final EntityDiscovery entityDiscovery;
    private final JpaEntityAnalyzer entityAnalyzer;
    private final OracleMetadataExtractor metadataExtractor;
    
    public SchemaComparisonService() {
        this.entityDiscovery = new EntityDiscovery();
        this.entityAnalyzer = new JpaEntityAnalyzer();
        this.metadataExtractor = new OracleMetadataExtractor();
    }
    
    public void compareSchemas(String classpath, String packageName, Connection dbConnection, String schemaName) throws Exception {
        log.info("Starting schema comparison...");
        
        // Discover and analyze JPA entities
        Set<Class<?>> entities = entityDiscovery.findEntitiesInPackage(classpath, packageName);
        Map<TableName, Set<ColumnName>> jpaSchema = analyzeJpaEntities(entities);
        
        // Extract database schema
        DatabaseSchema dbSchema = metadataExtractor.extractDatabaseSchema(dbConnection, schemaName);
        
        // Compare schemas
        compareAndReport(jpaSchema, dbSchema);
    }
    
    private Map<TableName, Set<ColumnName>> analyzeJpaEntities(Set<Class<?>> entities) {
        log.info("Analyzing {} JPA entities...", entities.size());
        
        Map<TableName, Set<ColumnName>> jpaSchema = new java.util.LinkedHashMap<>();
        
        for (Class<?> entity : entities) {
            try {
                Map<TableName, Set<ColumnName>> entitySchema = entityAnalyzer.analyzeEntity(entity);
                jpaSchema.putAll(entitySchema);
                log.debug("Analyzed entity: {} -> {} columns", entity.getSimpleName(), 
                    entitySchema.values().stream().mapToInt(Set::size).sum());
            } catch (Exception e) {
                log.error("Failed to analyze entity: {}", entity.getSimpleName(), e);
            }
        }
        
        return jpaSchema;
    }
    
    private void compareAndReport(Map<TableName, Set<ColumnName>> jpaSchema, DatabaseSchema dbSchema) {
        log.info("=== SCHEMA COMPARISON REPORT ===");
        
        Set<String> jpaTableNames = jpaSchema.keySet().stream()
            .map(tn -> tn.value().toUpperCase())
            .collect(java.util.stream.Collectors.toSet());
        
        Set<String> dbTableNames = dbSchema.tables().keySet().stream()
            .map(tn -> tn.value().toUpperCase())
            .collect(java.util.stream.Collectors.toSet());
        
        // Tables only in JPA
        Set<String> onlyInJpa = new HashSet<>(jpaTableNames);
        onlyInJpa.removeAll(dbTableNames);
        if (!onlyInJpa.isEmpty()) {
            log.warn("Tables defined in JPA but missing in database: {}", onlyInJpa);
        }
        
        // Tables only in DB
        Set<String> onlyInDb = new HashSet<>(dbTableNames);
        onlyInDb.removeAll(jpaTableNames);
        if (!onlyInDb.isEmpty()) {
            log.warn("Tables in database but not mapped by JPA: {}", onlyInDb);
        }
        
        // Compare columns for matching tables
        for (Map.Entry<TableName, Set<ColumnName>> jpaEntry : jpaSchema.entrySet()) {
            String tableName = jpaEntry.getKey().value().toUpperCase();
            Set<ColumnName> jpaColumns = jpaEntry.getValue();
            
            TableName dbTableName = dbSchema.tables().keySet().stream()
                .filter(tn -> tn.value().toUpperCase().equals(tableName))
                .findFirst()
                .orElse(null);
            
            if (dbTableName != null) {
                Set<ColumnName> dbColumns = dbSchema.tables().get(dbTableName);
                compareTableColumns(tableName, jpaColumns, dbColumns);
            }
        }
        
        log.info("=== END OF COMPARISON REPORT ===");
    }
    
    private void compareTableColumns(String tableName, Set<ColumnName> jpaColumns, Set<ColumnName> dbColumns) {
        log.info("Comparing columns for table: {}", tableName);
        
        Set<String> jpaColumnNames = jpaColumns.stream()
            .map(cn -> cn.value().toUpperCase())
            .collect(java.util.stream.Collectors.toSet());
        
        Set<String> dbColumnNames = dbColumns.stream()
            .map(cn -> cn.value().toUpperCase())
            .collect(java.util.stream.Collectors.toSet());
        
        // Columns only in JPA
        Set<String> onlyInJpa = new HashSet<>(jpaColumnNames);
        onlyInJpa.removeAll(dbColumnNames);
        if (!onlyInJpa.isEmpty()) {
            log.warn("  Columns in JPA but missing in DB: {}", onlyInJpa);
        }
        
        // Columns only in DB
        Set<String> onlyInDb = new HashSet<>(dbColumnNames);
        onlyInDb.removeAll(jpaColumnNames);
        if (!onlyInDb.isEmpty()) {
            log.warn("  Columns in DB but not in JPA: {}", onlyInDb);
        }
        
        // Matching columns
        Set<String> matching = new HashSet<>(jpaColumnNames);
        matching.retainAll(dbColumnNames);
        if (!matching.isEmpty()) {
            log.info("  Matching columns ({}): {}", matching.size(), matching);
        }
    }
}