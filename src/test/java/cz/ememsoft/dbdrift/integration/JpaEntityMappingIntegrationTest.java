package cz.ememsoft.dbdrift.integration;

import cz.ememsoft.dbdrift.config.DatabaseConfig;
import cz.ememsoft.dbdrift.db.H2MetadataExtractor;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.JpaSchema;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.parser.JpaSchemaParser;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integračný test, ktorý overuje, či program dokáže extrahovať z JPA entít 
 * presne tie isté atribúty ako sú v databáze.
 * 
 * Test proces:
 * 1. Vytvorí H2 databázu s presne definovanými tabuľkami a stĺpcami
 * 2. Sparsuje JPA entity z testovacích tried
 * 3. Porovná extrahované schémy - musia byť identické
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaEntityMappingIntegrationTest {
    
    private static final String H2_URL = "jdbc:h2:mem:testdb;MODE=Oracle;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";
    private static final String SCHEMA_NAME = "PUBLIC";
    
    private static DatabaseConfig testDbConfig;
    private static Path testEntitiesPath;
    
    @BeforeAll
    static void setupDatabase() throws Exception {
        // Inicializuj H2 databázu (použije sa dummy Oracle formát pre kompatibilitu)
        testDbConfig = new DatabaseConfig("localhost", 1521, "testdb", H2_USER, H2_PASSWORD, SCHEMA_NAME);
        
        // Vytvor schému databázy
        try (Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD)) {
            String schemaSql = Files.readString(
                Paths.get("src/test/resources/test-schema.sql")
            );
            
            try (Statement stmt = conn.createStatement()) {
                // Lepšie parsovanie SQL - odstráň komentáre a spracuj príkazy
                StringBuilder cleanSql = new StringBuilder();
                String[] lines = schemaSql.split("\\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    // Odstráň inline komentáre
                    int commentIndex = trimmedLine.indexOf("--");
                    if (commentIndex >= 0) {
                        trimmedLine = trimmedLine.substring(0, commentIndex).trim();
                    }
                    if (!trimmedLine.isEmpty()) {
                        cleanSql.append(" ").append(trimmedLine);
                    }
                }
                
                String[] statements = cleanSql.toString().split(";");
                for (String sql : statements) {
                    String trimmedSql = sql.trim();
                    if (!trimmedSql.isEmpty()) {
                        try {
                            stmt.execute(trimmedSql);
                            System.out.println("✅ Executed SQL statement successfully");
                        } catch (Exception e) {
                            System.err.println("❌ Failed to execute SQL:");
                            System.err.println(trimmedSql);
                            System.err.println("Error: " + e.getMessage());
                            throw e;
                        }
                    }
                }
                
                // Overenie že sa tabuľky vytvorili
                try (var rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'")) {
                    System.out.println("Vytvorené tabuľky:");
                    while (rs.next()) {
                        System.out.println("- " + rs.getString("TABLE_NAME"));
                    }
                }
            }
        }
        
        // Nastav cestu k testovacím entitám
        testEntitiesPath = Paths.get("src/test/java/cz/ememsoft/dbdrift/testentities");
    }
    
    @Test
    @Order(1)
    @DisplayName("Test extrakcie schémy z H2 databázy")
    void testDatabaseSchemaExtraction() {
        H2MetadataExtractor extractor = new H2MetadataExtractor(testDbConfig);
        DatabaseSchema dbSchema = extractor.fetchSchema();
        
        // Overenie že sa extrahovali všetky očakávané tabuľky
        Set<TableName> expectedTables = Set.of(
            new TableName("ANIMALS"),
            new TableName("VETERINARIANS"), 
            new TableName("CAT_TOYS"),
            new TableName("COMPLEX_ENTITIES"),
            new TableName("COMPLEX_ENTITY_DETAILS")
        );
        
        assertEquals(expectedTables, dbSchema.definition().tables().keySet(), 
            "Databázová schéma musí obsahovať všetky očakávané tabuľky");
        
        // Detailná kontrola Animal tabuľky (najkomplexnejšia)
        SortedSet<ColumnName> animalColumns = dbSchema.definition().tables().get(new TableName("ANIMALS"));
        assertNotNull(animalColumns, "ANIMALS tabuľka musí existovať");
        assertTrue(animalColumns.size() > 20, "ANIMALS tabuľka musí mať všetky stĺpce z dedičnosti");
        
        // Kontrola discriminator column
        assertTrue(animalColumns.contains(new ColumnName("ANIMAL_TYPE")), 
            "ANIMALS tabuľka musí obsahovať discriminator column");
    }
    
    @Test
    @Order(2) 
    @DisplayName("Test extrakcie schémy z JPA entít")
    void testJpaSchemaExtraction() {
        TestJpaSchemaParser parser = new TestJpaSchemaParser();
        JpaSchema jpaSchema = parser.parse(testEntitiesPath);
        
        // Overenie že sa extrahovali všetky entity
        Set<TableName> extractedTables = jpaSchema.definition().tables().keySet();
        assertTrue(extractedTables.size() >= 5, 
            "JPA schéma musí obsahovať všetky @Entity triedy");
        
        // Overenie konkrétnych tabuliek
        assertTrue(extractedTables.contains(new TableName("ANIMALS")), 
            "JPA schéma musí obsahovať ANIMALS tabuľku");
        assertTrue(extractedTables.contains(new TableName("VETERINARIANS")), 
            "JPA schéma musí obsahovať VETERINARIANS tabuľku");
    }
    
    @Test
    @Order(3)
    @DisplayName("Test zhody schém: databáza vs JPA entity")  
    void testSchemaConsistency() {
        // Extrahuj schémy z oboch zdrojov
        H2MetadataExtractor dbExtractor = new H2MetadataExtractor(testDbConfig);
        DatabaseSchema dbSchema = dbExtractor.fetchSchema();
        
        TestJpaSchemaParser jpaParser = new TestJpaSchemaParser();
        JpaSchema jpaSchema = jpaParser.parse(testEntitiesPath);
        
        // Porovnaj počet tabuliek
        assertEquals(dbSchema.definition().tables().size(), 
                     jpaSchema.definition().tables().size(),
            "Počet tabuliek musí byť rovnaký v databáze a JPA schéme");
        
        // Porovnaj každú tabuľku
        for (TableName tableName : dbSchema.definition().tables().keySet()) {
            assertTrue(jpaSchema.definition().tables().containsKey(tableName),
                "JPA schéma musí obsahovať tabuľku: " + tableName.value());
            
            SortedSet<ColumnName> dbColumns = dbSchema.definition().tables().get(tableName);
            SortedSet<ColumnName> jpaColumns = jpaSchema.definition().tables().get(tableName);
            
            assertEquals(dbColumns, jpaColumns,
                String.format("Stĺpce pre tabuľku %s sa musia zhodovať:\n" +
                    "Databáza: %s\nJPA:      %s", 
                    tableName.value(), dbColumns, jpaColumns));
        }
        
        System.out.println("✅ Schémy sú identické! Program správne extrahuje všetky atribúty.");
    }
    
    @Test
    @Order(4)
    @DisplayName("Test Spring Boot konvencií mapovaní")
    void testSpringBootNamingConventions() {
        TestJpaSchemaParser parser = new TestJpaSchemaParser();
        JpaSchema jpaSchema = parser.parse(testEntitiesPath);
        
        SortedSet<ColumnName> complexEntityColumns = 
            jpaSchema.definition().tables().get(new TableName("COMPLEX_ENTITIES"));
        
        assertNotNull(complexEntityColumns, "COMPLEX_ENTITIES tabuľka musí existovať");
        
        // Test rôznych Spring Boot konvencií
        assertTrue(complexEntityColumns.contains(new ColumnName("CAMEL_CASE")),
            "camelCase -> CAMEL_CASE");
        assertTrue(complexEntityColumns.contains(new ColumnName("XMLHTTP_REQUEST")),
            "XMLHttpRequest -> XMLHTTP_REQUEST");
        assertTrue(complexEntityColumns.contains(new ColumnName("HTML_PARSER")),
            "HTMLParser -> HTML_PARSER");
        assertTrue(complexEntityColumns.contains(new ColumnName("URL_PATH")),
            "URLPath -> URL_PATH");
        assertTrue(complexEntityColumns.contains(new ColumnName("USER_IDNUMBER")),
            "userIDNumber -> USER_IDNUMBER");
        assertTrue(complexEntityColumns.contains(new ColumnName("VERSION2_BETA3")),
            "version2Beta3 -> VERSION2_BETA3");
    }
    
    @Test 
    @Order(5)
    @DisplayName("Test dedičnosti a @Embedded mapovaní")
    void testInheritanceAndEmbeddedMappings() {
        TestJpaSchemaParser parser = new TestJpaSchemaParser();
        JpaSchema jpaSchema = parser.parse(testEntitiesPath);
        
        SortedSet<ColumnName> animalColumns = 
            jpaSchema.definition().tables().get(new TableName("ANIMALS"));
        
        assertNotNull(animalColumns, "ANIMALS tabuľka musí existovať");
        
        // Test BaseEntity dedičnosti
        assertTrue(animalColumns.contains(new ColumnName("ID")), 
            "Musí obsahovať ID z BaseEntity");
        assertTrue(animalColumns.contains(new ColumnName("VERSION")), 
            "Musí obsahovať VERSION z BaseEntity");
        assertTrue(animalColumns.contains(new ColumnName("CUSTOM_CREATED_AT")), 
            "Musí obsahovať CUSTOM_CREATED_AT z BaseEntity");
        
        // Test Animal polia  
        assertTrue(animalColumns.contains(new ColumnName("NAME")), 
            "Musí obsahovať NAME z Animal");
        assertTrue(animalColumns.contains(new ColumnName("SCIENTIFIC_NAME")), 
            "Musí obsahovať SCIENTIFIC_NAME z Animal");
        
        // Test Dog špecifických polí
        assertTrue(animalColumns.contains(new ColumnName("BREED")), 
            "Musí obsahovať BREED z Dog");
        assertTrue(animalColumns.contains(new ColumnName("IS_VACCINATED")), 
            "Musí obsahovať IS_VACCINATED z Dog");
        
        // Test Cat špecifických polí
        assertTrue(animalColumns.contains(new ColumnName("IS_INDOOR")), 
            "Musí obsahovať IS_INDOOR z Cat");
        assertTrue(animalColumns.contains(new ColumnName("FAVORITE_FOOD")), 
            "Musí obsahovať FAVORITE_FOOD z Cat");
        
        // Test @Embedded s AttributeOverrides
        assertTrue(animalColumns.contains(new ColumnName("HABITAT_STREET")), 
            "Musí obsahovať HABITAT_STREET z @AttributeOverride");
        assertTrue(animalColumns.contains(new ColumnName("HABITAT_CITY")), 
            "Musí obsahovať HABITAT_CITY z @AttributeOverride");
        
        // Test @Embedded s prefixom
        assertTrue(animalColumns.contains(new ColumnName("HOME_ADDRESS_STREET")), 
            "Musí obsahovať HOME_ADDRESS_STREET z Dog.homeAddress");
        assertTrue(animalColumns.contains(new ColumnName("HOME_ADDRESS_CITY_NAME")), 
            "Musí obsahovať HOME_ADDRESS_CITY_NAME s @Column mapovaním");
    }
}