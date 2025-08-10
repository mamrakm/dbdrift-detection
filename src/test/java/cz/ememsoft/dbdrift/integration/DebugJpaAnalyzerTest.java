package cz.ememsoft.dbdrift.integration;

import cz.ememsoft.dbdrift.jpa.JpaEntityAnalyzer;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.testentities.Individual;
import cz.ememsoft.dbdrift.testentities.PersonDocument;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DebugJpaAnalyzerTest {
    
    @Test
    void debugIndividualAnalysis() {
        JpaEntityAnalyzer analyzer = new JpaEntityAnalyzer();
        
        System.out.println("=== Analyzing Individual Entity ===");
        
        try {
            Map<TableName, Set<ColumnName>> result = analyzer.analyzeEntity(Individual.class);
            
            System.out.println("Result: " + result);
            
            if (result != null && !result.isEmpty()) {
                for (Map.Entry<TableName, Set<ColumnName>> entry : result.entrySet()) {
                    System.out.println("Table: " + entry.getKey().value());
                    Set<String> columnNames = entry.getValue().stream()
                        .map(col -> col.value().toUpperCase())
                        .collect(Collectors.toSet());
                    System.out.println("Columns (" + columnNames.size() + "): " + columnNames);
                }
            } else {
                System.out.println("Result is null or empty!");
            }
            
        } catch (Exception e) {
            System.err.println("Exception during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    void debugPersonDocumentAnalysis() {
        JpaEntityAnalyzer analyzer = new JpaEntityAnalyzer();
        
        System.out.println("=== Analyzing PersonDocument Entity ===");
        
        try {
            Map<TableName, Set<ColumnName>> result = analyzer.analyzeEntity(PersonDocument.class);
            
            System.out.println("Result: " + result);
            
            if (result != null && !result.isEmpty()) {
                for (Map.Entry<TableName, Set<ColumnName>> entry : result.entrySet()) {
                    System.out.println("Table: " + entry.getKey().value());
                    Set<String> columnNames = entry.getValue().stream()
                        .map(col -> col.value().toUpperCase())
                        .collect(Collectors.toSet());
                    System.out.println("Columns (" + columnNames.size() + "): " + columnNames);
                }
            } else {
                System.out.println("Result is null or empty!");
            }
            
        } catch (Exception e) {
            System.err.println("Exception during analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
}