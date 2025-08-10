package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete entity that extends Person.
 * Should inherit all fields from Person and AbstractAuditEntity.
 */
@Entity
@DiscriminatorValue("1")
public class Individual extends Person {
    
    public Individual() {
        super(PersonType.INDIVIDUAL);
    }
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "birth_number")
    private String birthNumber;
    
    // Getters and setters
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getBirthNumber() {
        return birthNumber;
    }
    
    public void setBirthNumber(String birthNumber) {
        this.birthNumber = birthNumber;
    }
    
    @Override
    public String getIdentifier() {
        return birthNumber != null ? birthNumber : (firstName + " " + lastName);
    }
}