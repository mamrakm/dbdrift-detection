package cz.ememsoft.dbdrift.parser;


import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import cz.ememsoft.dbdrift.exception.ApplicationExceptions;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.JpaSchema;
import cz.ememsoft.dbdrift.model.SchemaDefinition;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.parser.ast.EntityInheritanceGraph;
import cz.ememsoft.dbdrift.parser.ast.JpaEntityProcessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestruje parsovanie Java zdrojových súborov s cieľom extrahovať definíciu JPA schémy.
 */
@Slf4j
public class JpaSchemaParser {
    public JpaSchema parse(@NonNull Path sourceDir) {
        log.info("Spúšťam parsovanie JPA schémy z adresára: {}", sourceDir);
        List<Path> javaFiles = SourceFileScanner.findJavaFiles(sourceDir);
        log.info("Nájdených {} Java zdrojových súborov na spracovanie.", javaFiles.size());

        Map<Path, CompilationUnit> compilationUnits = javaFiles.parallelStream()
                .collect(Collectors.toConcurrentMap(Function.identity(), this::parseFile));

        var inheritanceGraph = new EntityInheritanceGraph(compilationUnits.values());
        var entityProcessor = new JpaEntityProcessor(inheritanceGraph);
        SortedMap<TableName, SortedSet<ColumnName>> tables = new TreeMap<>();

        inheritanceGraph.getEntities().forEach(entityDeclaration -> {
            var tableInfo = entityProcessor.processEntity(entityDeclaration);
            tables.put(tableInfo.getKey(), tableInfo.getValue());
        });

        log.info("Úspešne spracovaných {} JPA entít.", tables.size());
        return new JpaSchema(new SchemaDefinition(tables));
    }

    private CompilationUnit parseFile(Path file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (IOException | ParseProblemException e) {
            throw new ApplicationExceptions.JpaParsingException("Chyba pri parsovaní súboru: " + file, e);
        }
    }
}