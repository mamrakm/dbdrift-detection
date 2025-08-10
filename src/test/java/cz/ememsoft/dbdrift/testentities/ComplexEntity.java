package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Komplexná entita pre testovanie pokročilých JPA scenárov.
 * Kombinuje všetky typy anotácií a konvencií.
 */
@Entity
@Table(name = "COMPLEX_ENTITIES") 
public class ComplexEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Test všetkých Spring Boot konvencií
    private String camelCase;            // -> camel_case
    private String XMLHttpRequest;       // -> xmlhttp_request
    private String HTMLParser;           // -> html_parser
    private String URLPath;              // -> url_path
    private String userIDNumber;         // -> user_idnumber
    private String version2Beta3;        // -> version2_beta3
    private String getName;              // -> get_name
    private String setURL;               // -> set_url
    
    @Version
    private Integer optimisticLockVersion;
    
    @Column(name = "CUSTOM_NAME", nullable = false, length = 100)
    private String customNamedField;
    
    // Test embedded s komplexnými prefixmi
    @Embedded  
    private Address primaryAddress;      // prefix: primary_address_
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "BILLING_STREET_NAME")),
        @AttributeOverride(name = "city", column = @Column(name = "BILLING_CITY_NAME")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "BILLING_ZIP")),
        @AttributeOverride(name = "countryCode", column = @Column(name = "BILLING_COUNTRY"))
    })
    private Address billingAddress;
    
    // Test relačných polí
    @ManyToOne
    @JoinColumn(name = "ASSIGNED_VET_ID")
    private Veterinarian assignedVeterinarian;
    
    @OneToOne(mappedBy = "complexEntity")
    private ComplexEntityDetails details;
    
    // Timestamp polia (ak sú podporované)
    private LocalDateTime createdTimestamp;   // -> created_timestamp
    private LocalDateTime modifiedTimestamp;  // -> modified_timestamp
}