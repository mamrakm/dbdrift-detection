package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Test version of BooleanConverter for integration testing
 */
@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {
    
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute ? "A" : "N";
    }
    
    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return "A".equals(dbData);
    }
}