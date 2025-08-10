package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Test entity similar to DotDotaz structure.
 * - Extends BaseAuditEntity (which has @MappedSuperclass, NOT @Entity)
 * - Should inherit version, createdTime, modifiedTime fields
 * - Has discriminator column for inheritance
 * - Has complex field types and relationships
 */
@Entity
@Table(name = "query")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "query_type_code", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Query extends BaseAuditEntity {
    
    @Id
    @Column(name = "query_id", nullable = false)
    @SequenceGenerator(name = "querySeq", sequenceName = "seq_query", allocationSize = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "querySeq")
    private Long queryId;
    
    @Column(name = "query_type_code", insertable = false, updatable = false)
    @Enumerated(EnumType.ORDINAL)
    private QueryType queryType;
    
    @Column(name = "period_from", nullable = false)
    private LocalDate periodFrom;
    
    @Column(name = "period_to")
    private LocalDate periodTo;
    
    @Column(name = "status_code")
    @Enumerated(EnumType.ORDINAL) 
    private QueryStatus statusCode;
    
    @Column(name = "is_periodic", nullable = false)
    @Convert(converter = BooleanConverter.class)
    private Boolean isPeriodic;
    
    // Relationships (should be excluded from column mapping)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "query")
    private Set<QueryResponse> responses = new HashSet<>();
    
    // Getters and setters
    public Long getQueryId() {
        return queryId;
    }
    
    public QueryType getQueryType() {
        return queryType;
    }
    
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }
    
    public LocalDate getPeriodFrom() {
        return periodFrom;
    }
    
    public void setPeriodFrom(LocalDate periodFrom) {
        this.periodFrom = periodFrom;
    }
    
    public LocalDate getPeriodTo() {
        return periodTo;
    }
    
    public void setPeriodTo(LocalDate periodTo) {
        this.periodTo = periodTo;
    }
    
    public QueryStatus getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(QueryStatus statusCode) {
        this.statusCode = statusCode;
    }
    
    public Boolean getIsPeriodic() {
        return isPeriodic;
    }
    
    public void setIsPeriodic(Boolean isPeriodic) {
        this.isPeriodic = isPeriodic;
    }
    
    public Set<QueryResponse> getResponses() {
        return responses;
    }
}