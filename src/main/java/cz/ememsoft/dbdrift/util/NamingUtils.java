package cz.ememsoft.dbdrift.parser.util;

public final class NamingUtils {

    private NamingUtils() {}

    public static String classToTableName(String className) {
        return camelCaseToSnakeCase(className).toUpperCase();
    }

    public static String fieldToColumnName(String fieldName) {
        return camelCaseToSnakeCase(fieldName).toUpperCase();
    }

    private static String camelCaseToSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        // Vylepšený regex, ktorý správne handle-uje skratky ako "JPA" -> "JPA"
        return str.replaceAll("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", "_");
    }
}
