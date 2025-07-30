package cz.ememsoft.dbdrift.parser.ast;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import cz.ememsoft.dbdrift.model.ColumnName;
import cz.ememsoft.dbdrift.util.NameConverter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

/**
 * Extrahuje názov databázového stĺpca z deklarácie poľa v JPA entite.
 */
@Slf4j
public final class ColumnExtractor {
    private ColumnExtractor() {}

    public static Optional<ColumnName> extractColumnName(@NonNull FieldDeclaration field, @NonNull Map<String, String> overrides) {
        String fieldName = field.getVariable(0).getNameAsString();
        log.trace("-> Analyzujem pole '{}'. Overrides: {}", fieldName, overrides);

        if (overrides.containsKey(fieldName)) {
            String overriddenName = overrides.get(fieldName);
            log.trace("   Pole '{}' má prepísaný názov stĺpca na '{}'.", fieldName, overriddenName);
            return Optional.of(new ColumnName(overriddenName.toUpperCase()));
        }

        return findNameInAnnotation(field, "JoinColumn")
            .or(() -> findNameInAnnotation(field, "Column"))
            .or(() -> {
                if (field.isAnnotationPresent("ManyToOne") || field.isAnnotationPresent("OneToOne")) {
                    return Optional.of(NameConverter.camelToSnake(fieldName) + "_id");
                }
                return Optional.empty();
            })
            .or(() -> Optional.of(NameConverter.camelToSnake(fieldName)))
            .map(String::toUpperCase)
            .map(ColumnName::new);
    }

    private static Optional<String> findNameInAnnotation(FieldDeclaration field, String annotationName) {
        return field.getAnnotationByName(annotationName)
            .flatMap(ColumnExtractor::extractNameAttribute)
            .map(name -> {
                log.trace("   Nájdený názov '{}' v anotácii @{} poľa '{}'.", name, annotationName, field.getVariable(0).getNameAsString());
                return name;
            });
    }

    private static Optional<String> extractNameAttribute(AnnotationExpr annotation) {
        if (annotation instanceof NormalAnnotationExpr normalAnnotation) {
            return normalAnnotation.getPairs().stream()
                .filter(p -> "name".equals(p.getNameAsString()))
                .findFirst()
                .map(p -> p.getValue().asStringLiteralExpr().asString())
                .filter(name -> !name.isEmpty());
        }
        return Optional.empty();
    }
}