package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Detail entita pre testovanie OneToOne relácií.
 */
@Entity
@Table(name = "COMPLEX_ENTITY_DETAILS")
public class ComplexEntityDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String detailedDescription;    // -> detailed_description
    private String technicalSpecs;         // -> technical_specs
    private Boolean isActive;              // -> is_active
    
    @OneToOne
    @JoinColumn(name = "COMPLEX_ENTITY_ID")
    private ComplexEntity complexEntity;
}