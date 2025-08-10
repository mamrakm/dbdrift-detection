package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Concrete entity that extends Query.
 * Should inherit all fields from Query and BaseAuditEntity.
 */
@Entity
@DiscriminatorValue("1")
public class StandardQuery extends Query {
    
    public StandardQuery() {
        setQueryType(QueryType.PROPERTY_LEGAL_TITLES);
    }
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "priority_level")
    private Integer priorityLevel;
    
    // Getters and setters
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
}