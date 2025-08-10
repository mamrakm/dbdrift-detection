package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
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

import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity similar to SubSubjekt structure.
 * - Extends AbstractAuditEntity (which has @MappedSuperclass, NOT @Entity)
 * - Should inherit version, createdAt, lastModified fields
 * - Has discriminator column for inheritance
 */
@Entity
@Table(name = "person")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "person_type_code", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Person extends AbstractAuditEntity {
    
    protected Person() {
    }
    
    protected Person(PersonType personType) {
        this.personType = personType;
    }
    
    @Id
    @Column(name = "person_id")
    @SequenceGenerator(name = "personSeq", sequenceName = "seq_person", allocationSize = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "personSeq")
    private Long personId;
    
    @Column(name = "person_type_code", insertable = false, updatable = false)
    @Enumerated(EnumType.ORDINAL)
    private PersonType personType;
    
    @Column(name = "assigned_until")
    private LocalDateTime assignedUntil = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    
    @Column(name = "assignment_token")
    @JdbcType(VarcharJdbcType.class)
    private UUID assignmentToken = new UUID(0, 0);
    
    @Column(name = "evaluation_requested")
    private LocalDateTime evaluationRequested = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    
    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    
    // Relationships (should be excluded from column mapping)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
    private Set<PersonDocument> documents = new HashSet<>();
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
    private Set<PersonRequest> requests = new HashSet<>();
    
    // Getters and setters
    public Long getPersonId() {
        return personId;
    }
    
    public PersonType getPersonType() {
        return personType;
    }
    
    public LocalDateTime getAssignedUntil() {
        return assignedUntil;
    }
    
    public void setAssignedUntil(LocalDateTime assignedUntil) {
        this.assignedUntil = assignedUntil;
    }
    
    public UUID getAssignmentToken() {
        return assignmentToken;
    }
    
    public void setAssignmentToken(UUID assignmentToken) {
        this.assignmentToken = assignmentToken;
    }
    
    public LocalDateTime getEvaluationRequested() {
        return evaluationRequested;
    }
    
    public void setEvaluationRequested(LocalDateTime evaluationRequested) {
        this.evaluationRequested = evaluationRequested;
    }
    
    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }
    
    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public Set<PersonDocument> getDocuments() {
        return documents;
    }
    
    public Set<PersonRequest> getRequests() {
        return requests;
    }
    
    // Abstract method similar to getIdentifikator
    public abstract String getIdentifier();
}