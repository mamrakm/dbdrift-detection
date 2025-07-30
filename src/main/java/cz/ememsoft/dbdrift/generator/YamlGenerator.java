package cz.ememsoft.dbdrift.generator;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import cz.ememsoft.dbdrift.exception.ApplicationExceptions;
import cz.ememsoft.dbdrift.model.SchemaDefinition;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generuje kanonické YAML súbory z definícií schém.
 */
@Slf4j
public class YamlGenerator {
    private final ObjectMapper yamlMapper;
    public YamlGenerator() {
        YAMLFactory factory = new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
        this.yamlMapper = new ObjectMapper(factory).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public void writeToFile(@NonNull SchemaDefinition schemaDefinition, @NonNull Path outputPath) {
        try {
            log.info("Generujem YAML súbor na ceste: {}", outputPath);
            String yamlContent = yamlMapper.writeValueAsString(schemaDefinition.tables());
            Files.writeString(outputPath, yamlContent);
            log.info("Schéma bola úspešne zapísaná do {}.", outputPath);
        } catch (IOException e) {
            log.error("Nepodarilo sa zapísať YAML súbor do {}", outputPath, e);
            throw new ApplicationExceptions.FileGenerationException("Nebolo možné vygenerovať YAML súbor: " + outputPath, e);
        }
    }
}