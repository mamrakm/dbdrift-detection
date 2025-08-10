package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.*;

/**
 * Rodičovská entita pre testovanie @Inheritance hierarchie.
 * Používa SINGLE_TABLE stratégiu s discriminator column.
 */
@Entity
@Table(name = "ANIMALS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ANIMAL_TYPE", discriminatorType = DiscriminatorType.STRING)
public abstract class Animal extends BaseEntity {
    
    private String name;
    
    @Column(name = "SCIENTIFIC_NAME")
    private String species;
    
    private Integer age;
    private Double weight;      // -> weight
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "HABITAT_STREET")),
        @AttributeOverride(name = "city", column = @Column(name = "HABITAT_CITY"))
    })
    private Address habitat;
}