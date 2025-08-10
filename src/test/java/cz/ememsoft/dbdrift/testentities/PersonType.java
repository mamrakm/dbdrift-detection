package cz.ememsoft.dbdrift.testentities;

/**
 * Enum similar to SubCTypSubjektu
 */
public enum PersonType {
    INDIVIDUAL(1),
    COMPANY(2),
    ORGANIZATION(3);
    
    private final int code;
    
    PersonType(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}