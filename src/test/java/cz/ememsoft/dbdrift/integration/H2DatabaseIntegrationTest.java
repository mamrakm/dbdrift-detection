package cz.ememsoft.dbdrift.integration;

import cz.ememsoft.dbdrift.db.H2MetadataExtractor;
import cz.ememsoft.dbdrift.generator.YamlSchemaGenerator;
import cz.ememsoft.dbdrift.jpa.EntityDiscovery;
import cz.ememsoft.dbdrift.jpa.JpaEntityAnalyzer;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.testentities.StandardQuery;
import cz.ememsoft.dbdrift.testentities.QueryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration test that:
 * 1. Creates H2 in-memory database with real schema
 * 2. Analyzes JPA entities using reflection
 * 3. Extracts database schema metadata  
 * 4. Generates YAML files for both
 * 5. Compares results to verify program functionality
 * 
 * This test simulates the exact inheritance scenario from your real application:
 * StandardQuery -> Query (@Entity) -> BaseAuditEntity (@MappedSuperclass, NOT @Entity)
 */
public class H2DatabaseIntegrationTest {
    
    private Connection connection;
    private H2MetadataExtractor metadataExtractor;
    private JpaEntityAnalyzer entityAnalyzer;
    private YamlSchemaGenerator yamlGenerator;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create H2 in-memory database
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1", 
            "sa", 
            ""
        );
        
        // Execute schema setup script
        executeSchemaScript();
        
        // Initialize components
        metadataExtractor = new H2MetadataExtractor();
        entityAnalyzer = new JpaEntityAnalyzer();
        yamlGenerator = new YamlSchemaGenerator();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @Test
    void testCompleteIntegrationWithRealDatabaseSchema() throws Exception {
        System.out.println("=== STARTING H2 DATABASE INTEGRATION TEST ===");
        
        // Step 1: Verify database schema was created
        verifyDatabaseSchema();
        
        // Step 2: Analyze JPA entities  
        Map<TableName, Set<ColumnName>> jpaSchema = analyzeJpaEntities();
        
        // Step 3: Extract database schema
        DatabaseSchema dbSchema = metadataExtractor.extractDatabaseSchema(connection, "PUBLIC");
        
        // Step 4: Generate YAML files
        String jpaYamlPath = tempDir.resolve("real-jpa-schema.yaml").toString();
        String dbYamlPath = tempDir.resolve("real-database-schema.yaml").toString();
        
        yamlGenerator.generateJpaSchemaYaml(jpaSchema, jpaYamlPath);
        yamlGenerator.generateDatabaseSchemaYaml(dbSchema, dbYamlPath);
        
        // Step 5: Verify and compare results
        verifyJpaAnalysis(jpaSchema);
        verifyDatabaseAnalysis(dbSchema);
        verifyYamlFiles(jpaYamlPath, dbYamlPath);
        
        // Step 6: Test the critical inheritance scenario
        verifyInheritanceHandling(jpaSchema);
        
        System.out.println("=== INTEGRATION TEST COMPLETED SUCCESSFULLY ===");
    }
    
    private void executeSchemaScript() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/h2-integration-schema.sql")))) {
            
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("--")) {
                    sql.append(line).append("\n");
                    if (line.trim().endsWith(";")) {
                        try (Statement stmt = connection.createStatement()) {
                            stmt.execute(sql.toString());
                        }
                        sql.setLength(0);
                    }
                }
            }
        }
        
        System.out.println("Database schema created successfully");
    }
    
    private void verifyDatabaseSchema() throws Exception {
        // Verify QUERY table exists with expected columns
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'QUERY' ORDER BY COLUMN_NAME"
            );
            
            Set<String> actualColumns = new java.util.HashSet<>();
            while (rs.next()) {
                actualColumns.add(rs.getString("COLUMN_NAME").toUpperCase());
            }
            
            System.out.println("Database QUERY table columns: " + actualColumns);
            
            // Verify critical columns are present
            assertTrue(actualColumns.contains("QUERY_ID"), "Should have query_id");
            assertTrue(actualColumns.contains("QUERY_TYPE_CODE"), "Should have discriminator column");
            assertTrue(actualColumns.contains("VERSION"), "Should have version from BaseAuditEntity");
            assertTrue(actualColumns.contains("CREATED_TIME"), "Should have created_time from BaseAuditEntity");
            assertTrue(actualColumns.contains("MODIFIED_TIME"), "Should have modified_time from BaseAuditEntity");
            assertTrue(actualColumns.contains("PERIOD_FROM"), "Should have period_from from Query");
            assertTrue(actualColumns.contains("DESCRIPTION"), "Should have description from StandardQuery");
        }
    }
    
    private Map<TableName, Set<ColumnName>> analyzeJpaEntities() throws Exception {
        System.out.println("Analyzing JPA entities...");
        
        Map<TableName, Set<ColumnName>> jpaSchema = new java.util.LinkedHashMap<>();
        
        // Analyze StandardQuery (which extends Query -> BaseAuditEntity)
        Map<TableName, Set<ColumnName>> standardQuerySchema = entityAnalyzer.analyzeEntity(StandardQuery.class);
        jpaSchema.putAll(standardQuerySchema);
        
        // Analyze QueryResponse 
        Map<TableName, Set<ColumnName>> queryResponseSchema = entityAnalyzer.analyzeEntity(QueryResponse.class);
        jpaSchema.putAll(queryResponseSchema);
        
        System.out.println("JPA analysis completed. Found " + jpaSchema.size() + " tables:");
        jpaSchema.forEach((table, columns) -> {
            System.out.println("  " + table.value() + " (" + columns.size() + " columns)");
        });
        
        return jpaSchema;
    }
    
    private void verifyJpaAnalysis(Map<TableName, Set<ColumnName>> jpaSchema) {
        // Verify QUERY table from JPA analysis
        TableName queryTable = new TableName("query");
        assertTrue(jpaSchema.containsKey(queryTable), "JPA analysis should contain QUERY table");
        
        Set<String> jpaColumns = jpaSchema.get(queryTable).stream()
            .map(col -> col.value().toUpperCase())
            .collect(Collectors.toSet());
        
        System.out.println("JPA QUERY table columns: " + jpaColumns);
        
        // Verify inheritance: fields from BaseAuditEntity (@MappedSuperclass, NOT @Entity)
        assertTrue(jpaColumns.contains("VERSION"), "Should inherit version from BaseAuditEntity");
        assertTrue(jpaColumns.contains("CREATED_TIME"), "Should inherit created_time from BaseAuditEntity");
        assertTrue(jpaColumns.contains("MODIFIED_TIME"), "Should inherit modified_time from BaseAuditEntity");
        
        // Verify fields from Query (abstract @Entity)
        assertTrue(jpaColumns.contains("QUERY_ID"), "Should have query_id from Query");
        assertTrue(jpaColumns.contains("PERIOD_FROM"), "Should have period_from from Query");
        assertTrue(jpaColumns.contains("QUERY_TYPE_CODE"), "Should have discriminator column");
        
        // Verify fields from StandardQuery (concrete @Entity)
        assertTrue(jpaColumns.contains("DESCRIPTION"), "Should have description from StandardQuery");
        assertTrue(jpaColumns.contains("PRIORITY_LEVEL"), "Should have priority_level from StandardQuery");
        
        // Verify @OneToMany is excluded
        assertFalse(jpaColumns.contains("RESPONSES"), "Should NOT contain responses (@OneToMany)");
    }
    
    private void verifyDatabaseAnalysis(DatabaseSchema dbSchema) {
        Set<String> dbTableNames = dbSchema.tables().keySet().stream()
            .map(tn -> tn.value().toUpperCase())
            .collect(Collectors.toSet());
        
        System.out.println("Database tables: " + dbTableNames);
        assertTrue(dbTableNames.contains("QUERY"), "Database should contain QUERY table");
        assertTrue(dbTableNames.contains("QUERY_RESPONSE"), "Database should contain QUERY_RESPONSE table");
    }
    
    private void verifyYamlFiles(String jpaYamlPath, String dbYamlPath) throws Exception {
        // Verify YAML files were created and contain expected content
        assertTrue(Files.exists(Path.of(jpaYamlPath)), "JPA YAML file should exist");
        assertTrue(Files.exists(Path.of(dbYamlPath)), "Database YAML file should exist");
        
        String jpaYaml = Files.readString(Path.of(jpaYamlPath));
        String dbYaml = Files.readString(Path.of(dbYamlPath));
        
        System.out.println("\n=== JPA YAML CONTENT ===");
        System.out.println(jpaYaml);
        System.out.println("\n=== DATABASE YAML CONTENT ===");
        System.out.println(dbYaml);
        
        // Verify YAML structure
        assertTrue(jpaYaml.contains("JPA_ENTITIES:"), "JPA YAML should contain JPA_ENTITIES");
        assertTrue(dbYaml.contains("DATABASE_TABLES:"), "DB YAML should contain DATABASE_TABLES");
        
        // Verify both contain QUERY table
        assertTrue(jpaYaml.contains("QUERY:"), "JPA YAML should contain QUERY table");
        assertTrue(dbYaml.contains("QUERY:"), "DB YAML should contain QUERY table");
        
        // Verify critical inheritance fields are in both
        assertTrue(jpaYaml.contains("VERSION"), "JPA YAML should contain VERSION from inheritance");
        assertTrue(dbYaml.contains("VERSION"), "DB YAML should contain VERSION column");
    }
    
    private void verifyInheritanceHandling(Map<TableName, Set<ColumnName>> jpaSchema) {
        System.out.println("\n=== VERIFYING INHERITANCE HANDLING ===");
        
        TableName queryTable = new TableName("query");
        Set<ColumnName> columns = jpaSchema.get(queryTable);
        
        // Count columns from each level of inheritance
        Set<String> columnNames = columns.stream()
            .map(col -> col.value().toUpperCase())
            .collect(Collectors.toSet());
        
        System.out.println("Total columns in QUERY table: " + columns.size());
        System.out.println("Column names: " + columnNames);
        
        // Expected columns:
        // From BaseAuditEntity (3): version, created_time, modified_time
        // From Query (6): query_id, query_type_code, period_from, period_to, status_code, is_periodic  
        // From StandardQuery (2): description, priority_level
        // Total: 11 columns
        
        assertEquals(11, columns.size(), "Should have exactly 11 columns from inheritance hierarchy");
        
        // This is the CRITICAL test: verify fields from non-@Entity @MappedSuperclass are included
        assertTrue(columnNames.contains("VERSION"), "CRITICAL: Must inherit version from @MappedSuperclass");
        assertTrue(columnNames.contains("CREATED_TIME"), "CRITICAL: Must inherit created_time from @MappedSuperclass");  
        assertTrue(columnNames.contains("MODIFIED_TIME"), "CRITICAL: Must inherit modified_time from @MappedSuperclass");
        
        System.out.println("✅ INHERITANCE TEST PASSED: All fields from @MappedSuperclass correctly unified into entity table");
    }
}