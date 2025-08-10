package cz.ememsoft.dbdrift.exception;

/**
 * Jeden súbor obsahujúci všetky vlastné, logicky súvisiace výnimky aplikácie.
 */
public final class ApplicationExceptions {
    
    private ApplicationExceptions() {}
    
    public static class DatabaseConnectionException extends RuntimeException {
        public DatabaseConnectionException(String message, Throwable cause) { 
            super(message, cause); 
        }
    }
    
    public static class MetadataExtractionException extends RuntimeException {
        public MetadataExtractionException(String message, Throwable cause) { 
            super(message, cause); 
        }
    }
    
    public static class JpaParsingException extends RuntimeException {
        public JpaParsingException(String message) { 
            super(message); 
        }
        
        public JpaParsingException(String message, Throwable cause) { 
            super(message, cause); 
        }
    }
    
    public static class FileGenerationException extends RuntimeException {
        public FileGenerationException(String message, Throwable cause) { 
            super(message, cause); 
        }
    }
}