package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.*;

/**
 * Simple related entity for testing @OneToMany relationships
 */
@Entity
@Table(name = "person_document")
public class PersonDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "document_type")
    private String documentType;
    
    @Column(name = "document_number")
    private String documentNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private Person person;
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public String getDocumentType() {
        return documentType;
    }
    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    
    public String getDocumentNumber() {
        return documentNumber;
    }
    
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    
    public Person getPerson() {
        return person;
    }
    
    public void setPerson(Person person) {
        this.person = person;
    }
}