package cz.ememsoft.dbdrift.integration;

import cz.ememsoft.dbdrift.jpa.JpaEntityAnalyzer;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.testentities.Individual;
import cz.ememsoft.dbdrift.testentities.PersonDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that the JPA entity analyzer correctly handles
 * inheritance scenarios similar to SubSubjekt -> AbstractAuditedDataEntity
 */
public class JpaEntityInheritanceTest {
    
    private JpaEntityAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new JpaEntityAnalyzer();
    }
    
    @Test
    void testInheritanceFromMappedSuperclassWithoutEntityAnnotation() {
        // Analyze Individual entity which extends Person -> AbstractAuditEntity
        // AbstractAuditEntity is @MappedSuperclass (NOT @Entity)
        Map<TableName, Set<ColumnName>> result = analyzer.analyzeEntity(Individual.class);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Should create 'person' table (from @Table annotation)
        TableName expectedTableName = new TableName("person");
        assertTrue(result.containsKey(expectedTableName));
        
        Set<ColumnName> columns = result.get(expectedTableName);
        Set<String> columnNames = columns.stream()
            .map(col -> col.value().toUpperCase())
            .collect(Collectors.toSet());
        
        // Verify columns from Individual class
        assertTrue(columnNames.contains("PERSON_ID"), "Should contain person_id");
        assertTrue(columnNames.contains("PERSON_TYPE_CODE"), "Should contain discriminator column");
        assertTrue(columnNames.contains("FIRST_NAME"), "Should contain first_name");
        assertTrue(columnNames.contains("LAST_NAME"), "Should contain last_name");
        assertTrue(columnNames.contains("BIRTH_NUMBER"), "Should contain birth_number");
        
        // Verify columns from Person class
        assertTrue(columnNames.contains("ASSIGNED_UNTIL"), "Should contain assigned_until");
        assertTrue(columnNames.contains("ASSIGNMENT_TOKEN"), "Should contain assignment_token");
        assertTrue(columnNames.contains("EVALUATION_REQUESTED"), "Should contain evaluation_requested");
        assertTrue(columnNames.contains("EVALUATED_AT"), "Should contain evaluated_at");
        assertTrue(columnNames.contains("PUBLISHED_AT"), "Should contain published_at");
        
        // CRITICAL: Verify columns from AbstractAuditEntity (@MappedSuperclass, NOT @Entity)
        assertTrue(columnNames.contains("VERSION"), "Should contain version from AbstractAuditEntity");
        assertTrue(columnNames.contains("CREATED_AT"), "Should contain created_at from AbstractAuditEntity");
        assertTrue(columnNames.contains("LAST_MODIFIED"), "Should contain last_modified from AbstractAuditEntity");
        
        // Verify @OneToMany fields are excluded
        assertFalse(columnNames.contains("DOCUMENTS"), "Should NOT contain documents (@OneToMany)");
        assertFalse(columnNames.contains("REQUESTS"), "Should NOT contain requests (@OneToMany)");
        
        System.out.println("Found columns: " + columnNames);
        
        // Expected total: 3 (AbstractAuditEntity) + 5 (Person) + 4 (Individual) + 1 (discriminator) = 13 columns
        assertEquals(13, columns.size(), "Should have exactly 13 columns total");
    }
    
    @Test
    void testRelatedEntityAnalysis() {
        // Test that related entities are analyzed correctly
        Map<TableName, Set<ColumnName>> result = analyzer.analyzeEntity(PersonDocument.class);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        
        TableName expectedTableName = new TableName("person_document");
        assertTrue(result.containsKey(expectedTableName));
        
        Set<ColumnName> columns = result.get(expectedTableName);
        Set<String> columnNames = columns.stream()
            .map(col -> col.value().toUpperCase())
            .collect(Collectors.toSet());
        
        assertTrue(columnNames.contains("ID"), "Should contain id");
        assertTrue(columnNames.contains("DOCUMENT_TYPE"), "Should contain document_type");
        assertTrue(columnNames.contains("DOCUMENT_NUMBER"), "Should contain document_number");
        
        // @ManyToOne should create foreign key column
        assertTrue(columnNames.contains("PERSON_ID"), "Should contain person_id foreign key");
        
        // Should NOT contain the Person object itself
        assertFalse(columnNames.contains("PERSON"), "Should NOT contain person object");
        
        System.out.println("PersonDocument columns: " + columnNames);
    }
    
    @Test
    void testDiscriminatorColumnHandling() {
        Map<TableName, Set<ColumnName>> result = analyzer.analyzeEntity(Individual.class);
        
        Set<ColumnName> columns = result.get(new TableName("person"));
        Set<String> columnNames = columns.stream()
            .map(col -> col.value().toUpperCase())
            .collect(Collectors.toSet());
        
        // Should have discriminator column from @DiscriminatorColumn annotation
        assertTrue(columnNames.contains("PERSON_TYPE_CODE"), 
            "Should contain discriminator column person_type_code");
        
        // Should also have the field itself (which has insertable=false, updatable=false)
        // This demonstrates handling of discriminator column duplication
        long discriminatorCount = columnNames.stream()
            .filter(name -> name.equals("PERSON_TYPE_CODE"))
            .count();
        
        // Should appear only once (not duplicated)
        assertEquals(1, discriminatorCount, "Discriminator column should appear exactly once");
    }
}