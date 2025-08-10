package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

/**
 * Embedded trieda pre testovanie @Embedded mapovaní.
 * Testuje správne prefixovanie stĺpcov vo vložených objektoch.
 */
@Embeddable
public class Address {
    
    private String street;
    
    @Column(name = "CITY_NAME")
    private String city;
    
    private String zipCode;      // -> zip_code
    private String countryCode;  // -> country_code
}