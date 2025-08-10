package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.*;

/**
 * Potomok Animal entity pre testovanie dedičnosti.
 * Pridáva špecifické polia pre mačku.
 */
@Entity
@DiscriminatorValue("CAT")
public class Cat extends Animal {
    
    private Boolean isIndoor;        // -> is_indoor
    private String favoriteFood;     // -> favorite_food
    private Integer livesRemaining;  // -> lives_remaining
    
    @OneToOne
    @JoinColumn(name = "FAVORITE_TOY_ID")
    private CatToy favoriteToy;
}