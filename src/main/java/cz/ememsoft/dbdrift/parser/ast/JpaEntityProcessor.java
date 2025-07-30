package cz.ememsoft.dbdrift.parser.ast;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.util.NameConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spracováva jednu @Entity triedu, rekurzívne prechádza jej hierarchiou dedičnosti a vloženými objektmi.
 * Táto verzia obsahuje finálnu opravu pre správne generovanie prefixov pre @Embedded polia.
 */
@Slf4j
public class JpaEntityProcessor {
    private final EntityInheritanceGraph graph;

    public JpaEntityProcessor(@NonNull EntityInheritanceGraph graph) { this.graph = graph; }

    public Map.Entry<TableName, SortedSet<ColumnName>> processEntity(@NonNull ClassOrInterfaceDeclaration entity) {
        TableName tableName = TableNameExtractor.extractTableName(entity);
        SortedSet<ColumnName> columns = new TreeSet<>();
        log.debug("Spracovávam entitu: '{}' -> Tabuľka: '{}'", entity.getNameAsString(), tableName.value());
        collectColumns(entity, "", columns, Map.of());
        return new AbstractMap.SimpleImmutableEntry<>(tableName, columns);
    }

    private void collectColumns(@NonNull ClassOrInterfaceDeclaration currentClass, String prefix, @NonNull SortedSet<ColumnName> columns, @NonNull Map<String, String> overrides) {
        log.trace("Vstupujem do triedy '{}' s prefixom '{}' a {} prepismi.", currentClass.getNameAsString(), prefix, overrides.size());

        graph.getSuperclassOf(currentClass).ifPresent(superclass -> {
            Map<String, String> superclassOverrides = extractAttributeOverrides(currentClass);
            collectColumns(superclass, prefix, columns, superclassOverrides);
        });

        for (FieldDeclaration field : currentClass.getFields()) {
            if (!isMappableField(field)) continue;

            if (field.isAnnotationPresent("Embedded")) {
                processEmbedded(field, prefix, columns, overrides);
                continue;
            }

            ColumnExtractor.extractColumnName(field, overrides)
                    .ifPresent(colName -> columns.add(new ColumnName(prefix.toUpperCase() + colName.value())));
        }
    }

    private void processEmbedded(FieldDeclaration field, String prefix, SortedSet<ColumnName> columns, Map<String, String> parentOverrides) {
        String fieldName = field.getVariable(0).getNameAsString();
        log.trace("-> Nájdené vložené pole (Embedded): '{}'", fieldName);
        try {
            ResolvedType resolvedType = field.getElementType().resolve();

            if (resolvedType instanceof ResolvedReferenceType resolvedReferenceType) {
                String embeddableFqn = resolvedReferenceType.getQualifiedName();
                graph.findClassByFqn(embeddableFqn).ifPresent(embeddableClass -> {

                    // OPRAVA: Vytvorí nový prefix z názvu poľa, ktorý sa bude aplikovať na stĺpce vloženej triedy.
                    // Príklad: pole "homeAddress" vytvorí prefix "home_address_".
                    String newPrefix = prefix + NameConverter.camelToSnake(fieldName) + "_";

                    Map<String, String> embeddedOverrides = extractAttributeOverrides(field);
                    embeddedOverrides.putAll(parentOverrides);

                    log.trace("Rekurzívne volanie pre @Embedded triedu '{}' s novým prefixom '{}'", embeddableClass.getNameAsString(), newPrefix);
                    collectColumns(embeddableClass, newPrefix, columns, embeddedOverrides);
                });
            }
        } catch (UnsolvedSymbolException e) {
            log.warn("Nepodarilo sa vyriešiť typ pre @Embedded pole '{}' v triede '{}'. Stĺpce nebudú pridané.", fieldName, field.findAncestor(ClassOrInterfaceDeclaration.class).map(ClassOrInterfaceDeclaration::getNameAsString).orElse("?"));
        }
    }

    private Map<String, String> extractAttributeOverrides(NodeWithAnnotations<?> node) {
        return node.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals("AttributeOverrides") || a.getNameAsString().equals("AttributeOverride"))
                .flatMap(a -> {
                    if (a.isNormalAnnotationExpr() && a.getNameAsString().equals("AttributeOverrides")) {
                        return ((NormalAnnotationExpr) a).getPairs().stream()
                                .filter(p -> p.getNameAsString().equals("value"))
                                .map(MemberValuePair::getValue)
                                .filter(Expression::isArrayInitializerExpr)
                                .flatMap(e -> e.asArrayInitializerExpr().getValues().stream());
                    }
                    return Stream.of(a);
                })
                .map(expr -> (AnnotationExpr) expr)
                .map(this::parseSingleOverride)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
    }

    private Optional<Map.Entry<String, String>> parseSingleOverride(AnnotationExpr overrideAnnotation) {
        if (overrideAnnotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normal = overrideAnnotation.asNormalAnnotationExpr();
            Optional<String> name = normal.getPairs().stream()
                    .filter(p -> p.getNameAsString().equals("name")).findFirst()
                    .map(p -> p.getValue().asStringLiteralExpr().asString());

            Optional<String> columnName = normal.getPairs().stream()
                    .filter(p -> p.getNameAsString().equals("column")).findFirst()
                    .map(p -> p.getValue().asAnnotationExpr().asNormalAnnotationExpr())
                    .flatMap(columnAnn -> columnAnn.getPairs().stream()
                            .filter(p -> p.getNameAsString().equals("name")).findFirst()
                            .map(p -> p.getValue().asStringLiteralExpr().asString()));

            if (name.isPresent() && columnName.isPresent()) {
                return Optional.of(Map.entry(name.get(), columnName.get()));
            }
        }
        return Optional.empty();
    }

    private boolean isMappableField(FieldDeclaration field) {
        return !field.isStatic() && !field.isAnnotationPresent("Transient") && !field.isAnnotationPresent("OneToMany") && !field.isAnnotationPresent("ManyToMany");
    }
}