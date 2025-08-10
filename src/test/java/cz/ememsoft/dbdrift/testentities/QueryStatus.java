package cz.ememsoft.dbdrift.testentities;

/**
 * Test enum similar to DotCStavDotazu structure
 */
public enum QueryStatus {
    WAITING_FOR_PROCESSING(1),
    PROCESSING_IN_PROGRESS(2),
    COMPLETED_WITHOUT_ERROR(3),
    COMPLETED_WITH_ERROR(4);
    
    private final int code;
    
    QueryStatus(int code) {
        this.code = code;
    }
    
    public static QueryStatus fromCode(int code) {
        for (QueryStatus value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
    
    public int getCode() {
        return code;
    }
}