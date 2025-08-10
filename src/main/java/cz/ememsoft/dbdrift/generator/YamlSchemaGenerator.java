package cz.ememsoft.dbdrift.generator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.DatabaseSchema;
import cz.ememsoft.dbdrift.model.TableName;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class YamlSchemaGenerator {
    
    private final ObjectMapper yamlMapper;
    
    public YamlSchemaGenerator() {
        this.yamlMapper = new ObjectMapper(
            new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
        );
        this.yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    
    public void generateJpaSchemaYaml(Map<TableName, Set<ColumnName>> jpaSchema, String outputPath) throws IOException {
        log.info("Generating JPA schema YAML file: {}", outputPath);
        
        Map<String, Object> yamlData = createSortedYamlStructure(jpaSchema, "JPA_ENTITIES");
        
        File outputFile = new File(outputPath);
        yamlMapper.writeValue(outputFile, yamlData);
        
        log.info("JPA schema YAML generated successfully with {} tables", jpaSchema.size());
    }
    
    public void generateDatabaseSchemaYaml(DatabaseSchema dbSchema, String outputPath) throws IOException {
        log.info("Generating database schema YAML file: {}", outputPath);
        
        Map<String, Object> yamlData = createSortedYamlStructure(dbSchema.tables(), "DATABASE_TABLES");
        
        File outputFile = new File(outputPath);
        yamlMapper.writeValue(outputFile, yamlData);
        
        log.info("Database schema YAML generated successfully with {} tables", dbSchema.tables().size());
    }
    
    private Map<String, Object> createSortedYamlStructure(Map<TableName, Set<ColumnName>> schema, String rootKey) {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> tables = new LinkedHashMap<>();
        
        // Sort tables by name (uppercase)
        schema.entrySet().stream()
            .sorted(Map.Entry.comparingByKey((t1, t2) -> 
                t1.value().toUpperCase().compareTo(t2.value().toUpperCase())))
            .forEach(entry -> {
                String tableName = entry.getKey().value().toUpperCase();
                
                // Sort columns by name (uppercase)
                List<String> sortedColumns = entry.getValue().stream()
                    .map(col -> col.value().toUpperCase())
                    .sorted()
                    .collect(Collectors.toList());
                
                Map<String, Object> tableData = new LinkedHashMap<>();
                tableData.put("COLUMNS", sortedColumns);
                
                tables.put(tableName, tableData);
            });
        
        root.put(rootKey, tables);
        return root;
    }
}