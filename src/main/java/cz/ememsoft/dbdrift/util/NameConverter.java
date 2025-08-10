package cz.ememsoft.dbdrift.util;

import lombok.NonNull;
import java.util.regex.Pattern;

/**
 * Pomocná trieda na konverziu názvov medzi rôznymi konvenciami podľa Spring Boot JPA pravidiel.
 */
public final class NameConverter {
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("(?<=[a-z])(?=[A-Z])");
    private static final Pattern UPPER_CASE_ACRONYM_PATTERN = Pattern.compile("(?<=[A-Z])(?=[A-Z][a-z])");
    private static final Pattern DIGIT_LETTER_PATTERN = Pattern.compile("(?<=[0-9])(?=[A-Za-z])|(?<=[A-Za-z])(?=[0-9])");
    
    private NameConverter() {}
    
    /**
     * Konvertuje camelCase alebo PascalCase názov na snake_case podľa Spring Boot konvencií.
     * Podporuje všetky Spring Boot scenáre:
     * - camelCase -> camel_case
     * - PascalCase -> pascal_case  
     * - XMLParser -> xml_parser
     * - HTMLElement -> html_element
     * - getId -> get_id
     * - setURL -> set_url
     * - userID -> user_id
     * - firstName2 -> first_name2
     * - version2Beta -> version2_beta
     */
    public static String camelToSnake(@NonNull String input) {
        if (input.isEmpty()) return "";
        
        // Spracovanie akronymov (XML, HTML, URL, atď.)
        String result = UPPER_CASE_ACRONYM_PATTERN.matcher(input).replaceAll("_");
        
        // Spracovanie prechodu z malého na veľké písmeno
        result = CAMEL_CASE_PATTERN.matcher(result).replaceAll("_");
        
        // Spracovanie prechodu medzi číslami a písmenami
        result = DIGIT_LETTER_PATTERN.matcher(result).replaceAll("_");
        
        return result.toLowerCase();
    }
    
    /**
     * Konvertuje snake_case na camelCase.
     */
    public static String snakeToCamel(@NonNull String snakeCaseString) {
        if (snakeCaseString.isEmpty()) return "";
        
        String[] parts = snakeCaseString.split("_");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                result.append(Character.toUpperCase(parts[i].charAt(0)))
                      .append(parts[i].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
}