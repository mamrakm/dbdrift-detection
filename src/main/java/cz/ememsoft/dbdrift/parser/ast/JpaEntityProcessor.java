package cz.ememsoft.dbdrift.parser.ast;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.parser.util.NamingUtils;
import cz.ememsoft.dbdrift.util.AnnotationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class JpaEntityProcessor {

    private final EntityInheritanceGraph inheritanceGraph;

    public Map.Entry<TableName, SortedSet<ColumnName>> processEntity(ClassOrInterfaceDeclaration entity) {
        ClassOrInterfaceDeclaration root = findTableDefiningAncestorOrSelf(entity);

        String tableNameStr = AnnotationUtils.getTableName(root)
                .orElseGet(() -> NamingUtils.classToTableName(root.getNameAsString()));
        TableName tableName = new TableName(tableNameStr.toUpperCase());

        SortedSet<ColumnName> columns = new TreeSet<>();
        collectColumnsRecursively(entity, columns);

        return Map.entry(tableName, columns);
    }

    private ClassOrInterfaceDeclaration findTableDefiningAncestorOrSelf(ClassOrInterfaceDeclaration current) {
        Optional<ClassOrInterfaceDeclaration> parentEntity = current.getExtendedTypes().stream()
                .flatMap(superClassType -> {
                    try {
                        ResolvedType resolved = superClassType.resolve();
                        if (resolved.isReferenceType()) {
                            String qualifiedName = resolved.asReferenceType().getQualifiedName();
                            return inheritanceGraph.findClassByQualifiedName(qualifiedName).stream();
                        }
                        return Stream.empty();
                    } catch (UnsolvedSymbolException e) {
                        return Stream.empty();
                    }
                })
                .filter(cd -> cd.isAnnotationPresent("Entity"))
                .findFirst();

        return parentEntity.map(this::findTableDefiningAncestorOrSelf).orElse(current);
    }

    private void collectColumnsRecursively(ClassOrInterfaceDeclaration clazz, SortedSet<ColumnName> columns) {
        collectFieldsFromClass(clazz, columns);

        clazz.getExtendedTypes().forEach(superClassType -> {
            try {
                ResolvedType resolvedType = superClassType.resolve();
                if (resolvedType.isReferenceType()) {
                    String qualifiedName = resolvedType.asReferenceType().getQualifiedName();
                    inheritanceGraph.findClassByQualifiedName(qualifiedName)
                            .ifPresent(superClassDeclaration -> {
                                if (superClassDeclaration.isAnnotationPresent("MappedSuperclass") || superClassDeclaration.isAnnotationPresent("Entity")) {
                                    collectColumnsRecursively(superClassDeclaration, columns);
                                }
                            });
                }
            } catch (UnsolvedSymbolException e) {
                log.warn("Nepodarilo sa plne vyriešiť hierarchiu pre triedu '{}', pretože jej predok '{}' nebol nájdený v zdrojových kódoch. Atribúty z tohto predka a jeho rodičov nebudú zahrnuté.",
                        clazz.getNameAsString(), e.getName());
            }
        });
    }

    private void collectFieldsFromClass(ClassOrInterfaceDeclaration clazz, SortedSet<ColumnName> columns) {
        for (FieldDeclaration field : clazz.getFields()) {
            if (field.isStatic() || field.isAnnotationPresent("Transient")) {
                continue;
            }

            Optional<String> explicitColumnName = AnnotationUtils.getColumnName(field)
                    .or(() -> AnnotationUtils.getJoinColumnName(field));

            if (explicitColumnName.isPresent()) {
                columns.add(new ColumnName(explicitColumnName.get().toUpperCase()));
            } else {
                boolean isRelationship = field.isAnnotationPresent("OneToOne") ||
                        field.isAnnotationPresent("ManyToOne") ||
                        field.isAnnotationPresent("OneToMany") ||
                        field.isAnnotationPresent("ManyToMany");

                if (!isRelationship) {
                    String columnNameStr = NamingUtils.fieldToColumnName(field.getVariable(0).getNameAsString());
                    columns.add(new ColumnName(columnNameStr.toUpperCase()));
                }
            }
        }
    }
}