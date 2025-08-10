package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * Základná abstraktná entita pre testovanie dedičnosti.
 * Testuje mapovanie základných polí cez hierarchiu dedičnosti.
 */
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Version
    private Integer version;
    
    @Column(name = "CUSTOM_CREATED_AT")
    private java.time.LocalDateTime createdAt;
    
    // Test rôznych Spring Boot konvencií
    private String simpleField;           // -> simple_field
    private String XMLParser;             // -> xml_parser  
    private String HTMLElement;           // -> html_element
    private String userID;                // -> user_id
    private String firstName2;            // -> first_name2
    private String version2Beta;          // -> version2_beta
}