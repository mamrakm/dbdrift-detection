package cz.ememsoft.dbdrift.service;


import cz.ememsoft.dbdrift.config.DatabaseConfig;
import cz.ememsoft.dbdrift.db.OracleMetadataExtractor;
import cz.ememsoft.dbdrift.generator.YamlGenerator;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.JpaSchema;
import cz.ememsoft.dbdrift.model.Schema;
import cz.ememsoft.dbdrift.parser.JpaSchemaParser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Hlavná servisná trieda, ktorá orchestruje proces detekcie driftu.
 */
@Slf4j
public class DriftDetectionService {
    private final DatabaseConfig dbConfig;
    private final YamlGenerator yamlGenerator;

    public DriftDetectionService(@NonNull DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.yamlGenerator = new YamlGenerator();
    }

    public void detectAndReportDrift(@NonNull Path sourceDir) {
        log.info("Začínam extrakciu schémy z databázy...");
        DatabaseSchema dbSchema = new OracleMetadataExtractor(dbConfig).fetchSchema();

        log.info("Začínam extrakciu schémy z JPA entít...");
        JpaSchema jpaSchema = new JpaSchemaParser().parse(sourceDir);

        log.info("Generujem výstupné YAML súbory...");
        Stream.of(dbSchema, jpaSchema).forEach(this::generateReport);

        log.info("""
        
        ---------------------------------------------------------
        Proces bol dokončený. Boli vygenerované dva súbory:
        1. database_schema.yml (z Oracle DB)
        2. jpa_schema.yml (z JPA entít)
        
        Teraz ich môžete porovnať pomocou diff nástroja, napríklad:
        diff -u database_schema.yml jpa_schema.yml
        ---------------------------------------------------------
        """);
    }

    private void generateReport(Schema schema) {
        Path outputPath = getOutputPathFor(schema);
        yamlGenerator.writeToFile(schema.definition(), outputPath);
    }

    private Path getOutputPathFor(Schema schema) {
        return Paths.get(switch (schema) {
            case DatabaseSchema db -> "database_schema.yml";
            case JpaSchema jpa -> "jpa_schema.yml";
        });
    }
}