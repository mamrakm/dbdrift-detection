package cz.ememsoft.dbdrift.jpa;

import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.util.NameConverter;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class JpaEntityAnalyzer {
    
    public Map<TableName, Set<ColumnName>> analyzeEntity(Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not a JPA entity");
        }
        
        TableName tableName = extractTableName(entityClass);
        Set<ColumnName> columns = new LinkedHashSet<>();
        
        log.debug("Analyzing entity: {} -> Table: {}", entityClass.getSimpleName(), tableName.value());
        
        // Add discriminator column for inheritance hierarchies
        addDiscriminatorColumn(entityClass, columns);
        
        // Collect columns from the entity and its inheritance hierarchy
        collectColumns(entityClass, "", columns, new HashMap<>());
        
        return Map.of(tableName, columns);
    }
    
    private void collectColumns(Class<?> clazz, String prefix, Set<ColumnName> columns, Map<String, String> overrides) {
        log.trace("Processing class: {} with prefix: '{}'", clazz.getSimpleName(), prefix);
        
        // Process fields from current class
        for (Field field : clazz.getDeclaredFields()) {
            if (!isMappableField(field)) continue;
            
            if (field.isAnnotationPresent(Embedded.class)) {
                processEmbeddedField(field, prefix, columns, overrides);
                continue;
            }
            
            String columnName = extractColumnName(field, overrides);
            if (columnName != null) {
                String fullColumnName = prefix + columnName;
                log.trace("Adding column: '{}' from field '{}' in class '{}'", 
                    fullColumnName, field.getName(), clazz.getSimpleName());
                columns.add(new ColumnName(fullColumnName.toUpperCase()));
            }
        }
        
        // Process superclass if it exists and is not Object
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            Map<String, String> newOverrides = new HashMap<>(overrides);
            extractAttributeOverrides(clazz, newOverrides);
            collectColumns(superClass, prefix, columns, newOverrides);
        }
    }
    
    private void processEmbeddedField(Field field, String prefix, Set<ColumnName> columns, Map<String, String> parentOverrides) {
        String fieldName = field.getName();
        log.trace("Found embedded field: '{}'", fieldName);
        
        Class<?> embeddedType = field.getType();
        String newPrefix = prefix + NameConverter.camelToSnake(fieldName) + "_";
        
        Map<String, String> embeddedOverrides = new HashMap<>(parentOverrides);
        extractAttributeOverrides(field, embeddedOverrides);
        
        log.trace("Recursively processing @Embedded class '{}' with prefix '{}'", 
            embeddedType.getSimpleName(), newPrefix);
        collectColumns(embeddedType, newPrefix, columns, embeddedOverrides);
    }
    
    private String extractColumnName(Field field, Map<String, String> overrides) {
        String fieldName = field.getName();
        
        // Check for override first
        if (overrides.containsKey(fieldName)) {
            return overrides.get(fieldName);
        }
        
        // Check for @Column annotation
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                return column.name();
            }
        }
        
        // Use Spring Boot naming convention (camelCase -> snake_case)
        return NameConverter.camelToSnake(fieldName);
    }
    
    private TableName extractTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                return new TableName(table.name());
            }
        }
        
        // Use Spring Boot naming convention (CamelCase -> snake_case)
        return new TableName(NameConverter.camelToSnake(entityClass.getSimpleName()));
    }
    
    private void addDiscriminatorColumn(Class<?> entityClass, Set<ColumnName> columns) {
        if (!entityClass.isAnnotationPresent(Inheritance.class)) {
            return;
        }
        
        String discriminatorColumnName = "DTYPE"; // JPA default
        
        if (entityClass.isAnnotationPresent(DiscriminatorColumn.class)) {
            DiscriminatorColumn discriminatorColumn = entityClass.getAnnotation(DiscriminatorColumn.class);
            if (!discriminatorColumn.name().isEmpty()) {
                discriminatorColumnName = discriminatorColumn.name();
            }
        }
        
        log.trace("Adding discriminator column '{}' for entity '{}'", discriminatorColumnName, entityClass.getSimpleName());
        columns.add(new ColumnName(discriminatorColumnName.toUpperCase()));
    }
    
    private void extractAttributeOverrides(Object source, Map<String, String> overrides) {
        Class<?> sourceClass = (source instanceof Class) ? (Class<?>) source : source.getClass();
        
        if (sourceClass.isAnnotationPresent(AttributeOverrides.class)) {
            AttributeOverrides attributeOverrides = sourceClass.getAnnotation(AttributeOverrides.class);
            for (AttributeOverride override : attributeOverrides.value()) {
                overrides.put(override.name(), override.column().name());
            }
        }
        
        if (sourceClass.isAnnotationPresent(AttributeOverride.class)) {
            AttributeOverride override = sourceClass.getAnnotation(AttributeOverride.class);
            overrides.put(override.name(), override.column().name());
        }
    }
    
    private boolean isMappableField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) 
            && !Modifier.isFinal(field.getModifiers())
            && !field.isAnnotationPresent(Transient.class)
            && !field.isAnnotationPresent(OneToMany.class)
            && !field.isAnnotationPresent(ManyToMany.class);
    }
}