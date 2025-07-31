package cz.ememsoft.dbdrift.util;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.Optional;

public final class AnnotationUtils {

    private AnnotationUtils() {}

    public static Optional<String> getTableName(ClassOrInterfaceDeclaration entity) {
        return entity.getAnnotationByName("Table")
                .flatMap(annotation -> getAnnotationAttributeValue(annotation, "name"));
    }

    public static Optional<String> getColumnName(FieldDeclaration field) {
        return field.getAnnotationByName("Column")
                .flatMap(annotation -> getAnnotationAttributeValue(annotation, "name"));
    }

    public static Optional<String> getJoinColumnName(FieldDeclaration field) {
        return field.getAnnotationByName("JoinColumn")
                .flatMap(annotation -> getAnnotationAttributeValue(annotation, "name"));
    }

    private static Optional<String> getAnnotationAttributeValue(AnnotationExpr annotation, String attributeName) {
        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) annotation;
            return normalAnnotation.getPairs().stream()
                    .filter(pair -> attributeName.equals(pair.getNameAsString()))
                    .findFirst()
                    .map(MemberValuePair::getValue)
                    .filter(expr -> expr.isStringLiteralExpr())
                    .map(expr -> ((StringLiteralExpr) expr).getValue());
        }
        if (annotation.isSingleMemberAnnotationExpr() && "name".equals(attributeName)) {
            var memberValue = ((com.github.javaparser.ast.expr.SingleMemberAnnotationExpr) annotation).getMemberValue();
            if(memberValue.isStringLiteralExpr()){
                return Optional.of(((StringLiteralExpr) memberValue).getValue());
            }
        }
        return Optional.empty();
    }
}