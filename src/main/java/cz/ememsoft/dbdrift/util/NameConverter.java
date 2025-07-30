package cz.ememsoft.dbdrift.util;

import lombok.NonNull;
import java.util.regex.Pattern;

/**
 * Pomocná trieda na konverziu názvov medzi rôznymi konvenciami.
 */
public final class NameConverter {
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(?<=[a-z])(?=[A-Z])");
    private NameConverter() {}
    public static String camelToSnake(@NonNull String camelCaseString) {
        if (camelCaseString.isEmpty()) return "";
        return CAMEL_CASE_PATTERN.matcher(camelCaseString).replaceAll("_").toLowerCase();
    }
}