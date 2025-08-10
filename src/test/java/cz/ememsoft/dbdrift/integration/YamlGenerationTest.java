package cz.ememsoft.dbdrift.integration;

import cz.ememsoft.dbdrift.jpa.EntityDiscovery;
import cz.ememsoft.dbdrift.jpa.JpaEntityAnalyzer;
import cz.ememsoft.dbdrift.generator.YamlSchemaGenerator;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.TableName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for YAML generation functionality
 */
public class YamlGenerationTest {
    
    private EntityDiscovery entityDiscovery;
    private JpaEntityAnalyzer entityAnalyzer;
    private YamlSchemaGenerator yamlGenerator;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        entityDiscovery = new EntityDiscovery();
        entityAnalyzer = new JpaEntityAnalyzer();
        yamlGenerator = new YamlSchemaGenerator();
    }
    
    @Test
    void testEntityDiscoveryAndYamlGeneration() throws Exception {
        // Discover test entities from classpath
        String testClasspath = "target/test-classes";
        String packageName = "cz.ememsoft.dbdrift.testentities";
        
        Set<Class<?>> entities = entityDiscovery.findEntitiesInPackage(testClasspath, packageName);
        
        assertNotNull(entities);
        assertTrue(entities.size() > 0, "Should find at least one entity");
        
        System.out.println("Found entities: " + entities.size());
        entities.forEach(entity -> System.out.println("  - " + entity.getSimpleName()));
        
        // Analyze entities
        Map<TableName, Set<ColumnName>> jpaSchema = analyzeEntities(entities);
        
        assertNotNull(jpaSchema);
        assertTrue(jpaSchema.size() > 0, "Should analyze at least one entity");
        
        // Generate YAML
        String yamlPath = tempDir.resolve("test-jpa-schema.yaml").toString();
        yamlGenerator.generateJpaSchemaYaml(jpaSchema, yamlPath);
        
        // Verify YAML file was created
        File yamlFile = new File(yamlPath);
        assertTrue(yamlFile.exists(), "YAML file should be created");
        assertTrue(yamlFile.length() > 0, "YAML file should not be empty");
        
        // Read and verify YAML content
        String yamlContent = Files.readString(yamlFile.toPath());
        System.out.println("Generated YAML content:");
        System.out.println(yamlContent);
        
        // Verify YAML structure
        assertTrue(yamlContent.contains("JPA_ENTITIES:"), "Should contain JPA_ENTITIES root");
        assertTrue(yamlContent.contains("COLUMNS:"), "Should contain COLUMNS");
        
        // Verify our test entities are included
        assertTrue(yamlContent.contains("PERSON:") || yamlContent.contains("INDIVIDUAL:"), 
            "Should contain Person or Individual entity");
        assertTrue(yamlContent.contains("PERSON_DOCUMENT:"), "Should contain PersonDocument entity");
        
        // Verify inheritance fields are included
        assertTrue(yamlContent.contains("VERSION"), "Should contain version from AbstractAuditEntity");
        assertTrue(yamlContent.contains("CREATED_AT"), "Should contain created_at from AbstractAuditEntity");
        assertTrue(yamlContent.contains("LAST_MODIFIED"), "Should contain last_modified from AbstractAuditEntity");
    }
    
    private Map<TableName, Set<ColumnName>> analyzeEntities(Set<Class<?>> entities) {
        Map<TableName, Set<ColumnName>> jpaSchema = new java.util.LinkedHashMap<>();
        
        for (Class<?> entity : entities) {
            try {
                Map<TableName, Set<ColumnName>> entitySchema = entityAnalyzer.analyzeEntity(entity);
                jpaSchema.putAll(entitySchema);
                System.out.println("Analyzed " + entity.getSimpleName() + " -> " + 
                    entitySchema.values().stream().mapToInt(Set::size).sum() + " columns");
            } catch (Exception e) {
                System.err.println("Failed to analyze entity: " + entity.getSimpleName() + " - " + e.getMessage());
            }
        }
        
        return jpaSchema;
    }
}