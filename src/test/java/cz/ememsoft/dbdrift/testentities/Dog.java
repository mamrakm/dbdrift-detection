package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Potomok Animal entity pre testovanie dedičnosti.
 * Pridáva špecifické polia pre psa.
 */
@Entity
@DiscriminatorValue("DOG")
public class Dog extends Animal {
    
    private String breed;
    private Boolean isVaccinated;    // -> is_vaccinated
    private String ownerID;          // -> owner_id
    
    @ManyToOne
    @JoinColumn(name = "VETERINARIAN_ID")
    private Veterinarian veterinarian;
    
    @Embedded
    private Address homeAddress;     // prefix: home_address_
}