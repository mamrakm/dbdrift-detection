package cz.ememsoft.dbdrift.testentities;

/**
 * Test enum similar to DotCTypDotazu structure
 */
public enum QueryType {
    PROPERTY_LEGAL_TITLES(10),
    PROPERTY_OWNED_BY_PERSONS(11), 
    CLIENT_DOCUMENT_PROOF(20),
    AGENT_CONFLICT_RESOLUTION(30),
    ENERGY_GAS_COLLECTION(40),
    ENERGY_ELECTRICITY_COLLECTION(50);
    
    private final int code;
    
    QueryType(int code) {
        this.code = code;
    }
    
    public static QueryType fromCode(int code) {
        for (QueryType value : values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
    
    public int getCode() {
        return code;
    }
    
    public static class Values {
        private Values() {}
        
        public static final String PROPERTY_LEGAL_TITLES = "10";
        public static final String PROPERTY_OWNED_BY_PERSONS = "11";
        public static final String CLIENT_DOCUMENT_PROOF = "20";
        public static final String AGENT_CONFLICT_RESOLUTION = "30";
        public static final String ENERGY_GAS_COLLECTION = "40";
        public static final String ENERGY_ELECTRICITY_COLLECTION = "50";
    }
}