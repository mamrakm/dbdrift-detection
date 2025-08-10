package cz.ememsoft.dbdrift.service;

import cz.ememsoft.dbdrift.db.OracleMetadataExtractor;
import cz.ememsoft.dbdrift.generator.YamlSchemaGenerator;
import cz.ememsoft.dbdrift.jpa.EntityDiscovery;
import cz.ememsoft.dbdrift.jpa.JpaEntityAnalyzer;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.TableName;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SchemaComparisonService {
    
    private final EntityDiscovery entityDiscovery;
    private final JpaEntityAnalyzer entityAnalyzer;
    private final OracleMetadataExtractor metadataExtractor;
    private final YamlSchemaGenerator yamlGenerator;
    
    public SchemaComparisonService() {
        this.entityDiscovery = new EntityDiscovery();
        this.entityAnalyzer = new JpaEntityAnalyzer();
        this.metadataExtractor = new OracleMetadataExtractor();
        this.yamlGenerator = new YamlSchemaGenerator();
    }
    
    public void generateSchemaYamls(String classpath, String packageName, Connection dbConnection, String schemaName) throws Exception {
        log.info("Starting schema YAML generation...");
        
        // Discover and analyze JPA entities
        Set<Class<?>> entities = entityDiscovery.findEntitiesInPackage(classpath, packageName);
        Map<TableName, Set<ColumnName>> jpaSchema = analyzeJpaEntities(entities);
        
        // Extract database schema
        DatabaseSchema dbSchema = metadataExtractor.extractDatabaseSchema(dbConnection, schemaName);
        
        // Generate YAML files
        generateYamlFiles(jpaSchema, dbSchema);
        
        log.info("Schema YAML files generated successfully. Use diff tool to compare:");
        log.info("  diff jpa-schema.yaml database-schema.yaml");
    }
    
    private Map<TableName, Set<ColumnName>> analyzeJpaEntities(Set<Class<?>> entities) {
        log.info("Analyzing {} JPA entities...", entities.size());
        
        Map<TableName, Set<ColumnName>> jpaSchema = new LinkedHashMap<>();
        
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
    
    private void generateYamlFiles(Map<TableName, Set<ColumnName>> jpaSchema, DatabaseSchema dbSchema) throws Exception {
        // Generate JPA schema YAML
        yamlGenerator.generateJpaSchemaYaml(jpaSchema, "jpa-schema.yaml");
        
        // Generate database schema YAML
        yamlGenerator.generateDatabaseSchemaYaml(dbSchema, "database-schema.yaml");
        
        log.info("Generated YAML files:");
        log.info("  - jpa-schema.yaml ({} tables)", jpaSchema.size());
        log.info("  - database-schema.yaml ({} tables)", dbSchema.tables().size());
    }
}