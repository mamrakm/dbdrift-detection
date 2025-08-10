package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDate;

/**
 * Samostatná entita pre testovanie relačných mapovaní.
 * Testuje @ManyToOne spojenia a rôzne JPA anotácie.
 */
@Entity
@Table(name = "VETERINARIANS")
public class Veterinarian {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String firstName;        // -> first_name
    private String lastName;         // -> last_name
    private String licenseNumber;    // -> license_number
    
    @Column(name = "PHONE_NUM")
    private String phoneNumber;
    
    private LocalDate graduationDate; // -> graduation_date
    
    @Embedded
    private Address clinicAddress;   // prefix: clinic_address_
    
    // Testuje transient pole (nemá byť mapované)
    @Transient
    private String temporaryNote;
}