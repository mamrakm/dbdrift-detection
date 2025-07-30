package cz.ememsoft.dbdrift.parser.ast;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import cz.ememsoft.dbdrift.model.TableName;
import cz.ememsoft.dbdrift.util.NameConverter;
import lombok.NonNull;

import java.util.Optional;

/**
 * Extrahuje názov databázovej tabuľky z deklarácie triedy JPA entity.
 */
public final class TableNameExtractor {
    private TableNameExtractor() {}

    public static TableName extractTableName(@NonNull ClassOrInterfaceDeclaration entityDeclaration) {
        return entityDeclaration.getAnnotationByName("Table")
                .flatMap(TableNameExtractor::extractNameAttribute)
                .orElseGet(() -> NameConverter.camelToSnake(entityDeclaration.getNameAsString()))
                .toUpperCase()
                .transform(TableName::new);
    }

    private static Optional<String> extractNameAttribute(AnnotationExpr annotation) {
        if (annotation instanceof NormalAnnotationExpr normal) {
            return normal.getPairs().stream()
                    .filter(p -> "name".equals(p.getNameAsString()))
                    .findFirst()
                    .map(p -> p.getValue().asStringLiteralExpr().asString())
                    .filter(name -> !name.isEmpty());
        }
        return Optional.empty();
    }
}