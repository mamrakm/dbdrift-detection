package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entita pre testovanie @OneToOne relácií.
 */
@Entity 
@Table(name = "CAT_TOYS")
public class CatToy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String material;
    private Double price;
    private Boolean isSqueaky;       // -> is_squeaky
}