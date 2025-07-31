package cz.ememsoft.dbdrift.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import cz.ememsoft.dbdrift.exception.ApplicationExceptions.JpaParsingException;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.JpaSchema;
import cz.ememsoft.dbdrift.model.SchemaDefinition;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.parser.ast.EntityInheritanceGraph;
import cz.ememsoft.dbdrift.parser.ast.JpaEntityProcessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class JpaSchemaParser {

    public JpaSchema parse(@NonNull List<Path> sourceDirs) {
        log.info("Spúšťam parsovanie JPA schémy z nasledujúcich adresárov: {}", sourceDirs);

        List<Path> sourceRoots = sourceDirs.stream()
                .map(this::findSourceRoot)
                .distinct()
                .collect(Collectors.toList());

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        sourceRoots.forEach(root -> {
            log.info("Pridávam zdrojový adresár '{}' do Symbol Resolvera.", root);
            combinedTypeSolver.add(new JavaParserTypeSolver(root));
        });

        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
                .setCharacterEncoding(StandardCharsets.UTF_8)
                .setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));

        JavaParser javaParser = new JavaParser(parserConfiguration);

        List<Path> javaFiles = sourceRoots.stream()
                .flatMap(root -> SourceFileScanner.findJavaFiles(root).stream())
                .distinct()
                .collect(Collectors.toList());

        log.info("Nájdených {} unikátnych Java zdrojových súborov na spracovanie zo všetkých modulov.", javaFiles.size());

        Map<Path, CompilationUnit> compilationUnits = javaFiles.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        file -> parseFile(file, javaParser)
                ));

        var inheritanceGraph = new EntityInheritanceGraph(compilationUnits.values());
        var entityProcessor = new JpaEntityProcessor(inheritanceGraph);
        SortedMap<TableName, SortedSet<ColumnName>> tables = new TreeMap<>();

        // === KĽÚČOVÁ ZMENA LOGIKY ===
        // Spracujeme každú konkrétnu entitu. Procesor sa postará o správne priradenie k tabuľke
        // a zlúčenie stĺpcov v rámci SINGLE_TABLE hierarchie.
        inheritanceGraph.getEntities().forEach(entityDeclaration -> {
            var tableInfo = entityProcessor.processEntity(entityDeclaration);

            // Zlúčime stĺpce, ak tabuľka už existuje (pre SINGLE_TABLE dedičnosť)
            tables.merge(tableInfo.getKey(), tableInfo.getValue(), (existingColumns, newColumns) -> {
                existingColumns.addAll(newColumns);
                return existingColumns;
            });
        });

        log.info("Vyhľadávam a spracovávam číselníky (enumy)...");
        compilationUnits.values().forEach(cu ->
                cu.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
                    if (isCodeProviderEnum(enumDeclaration)) {
                        var tableInfo = processEnumAsTable(enumDeclaration);
                        if (tableInfo != null) {
                            log.debug("Nájdený a spracovaný číselník: {}", tableInfo.getKey());
                            tables.put(tableInfo.getKey(), tableInfo.getValue());
                        }
                    }
                })
        );

        log.info("Úspešne spracovaných {} JPA entít a číselníkov.", tables.size());
        return new JpaSchema(new SchemaDefinition(tables));
    }

    private CompilationUnit parseFile(Path file, JavaParser javaParser) {
        try {
            ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
            if (parseResult.getResult().isPresent()) {
                if (!parseResult.isSuccessful()) {
                    log.warn("Súbor '{}' bol spracovaný, ale obsahuje nasledujúce problémy:", file);
                    parseResult.getProblems().forEach(problem -> log.warn("  - {}", problem.getVerboseMessage()));
                }
                return parseResult.getResult().get();
            } else {
                log.error("Fatálna chyba pri parsovaní súboru '{}'. Nebolo možné vytvoriť AST.", file);
                List<Problem> problems = parseResult.getProblems();
                problems.forEach(problem -> log.error("  - {}", problem.getVerboseMessage()));
                String problemSummary = problems.stream().map(Problem::getMessage).collect(Collectors.joining("; "));
                throw new JpaParsingException("Parser zlyhal pre súbor: " + file + ". Dôvody: " + problemSummary);
            }
        } catch (IOException e) {
            throw new JpaParsingException("Chyba pri čítaní súboru: " + file, e);
        }
    }

    private boolean isCodeProviderEnum(EnumDeclaration enumDeclaration) {
        return enumDeclaration.getImplementedTypes().stream()
                .anyMatch(implemented -> implemented.getNameAsString().equals("CodeProvider"));
    }

    private Map.Entry<TableName, SortedSet<ColumnName>> processEnumAsTable(EnumDeclaration enumDeclaration) {
        try {
            String enumName = enumDeclaration.getNameAsString();
            String tableNameStr = enumName
                    .replaceAll("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", "_")
                    .toUpperCase()
                    .replaceFirst("_(C)_", "_C_");
            TableName tableName = new TableName(tableNameStr);
            String codeColumnNameStr = tableNameStr.substring(tableNameStr.indexOf("_C_") + 3) + "_KOD";
            SortedSet<ColumnName> columns = new TreeSet<>();
            columns.add(new ColumnName(codeColumnNameStr));
            columns.add(new ColumnName("TEXT"));
            return Map.entry(tableName, columns);
        } catch (Exception e) {
            log.error("Chyba pri spracovaní enumu '{}' ako tabuľky.", enumDeclaration.getNameAsString(), e);
            return null;
        }
    }

    private Path findSourceRoot(Path initialPath) {
        Path current = initialPath.toAbsolutePath();
        while (current != null) {
            if (current.endsWith("src/main/java")) {
                return current;
            }
            Path potentialRoot = current.resolve("src/main/java");
            if (Files.isDirectory(potentialRoot)) {
                return potentialRoot;
            }
            current = current.getParent();
        }
        log.warn("Nepodarilo sa nájsť štandardný koreňový adresár 'src/main/java' nad '{}'. Používam zadanú cestu.", initialPath);
        return initialPath;
    }
}