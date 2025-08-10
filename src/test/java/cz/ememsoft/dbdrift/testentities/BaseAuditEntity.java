package cz.ememsoft.dbdrift.testentities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Test version of AbstractAuditedDataEntity for integration testing.
 * This class is NOT annotated with @Entity - only @MappedSuperclass.
 * Simulates the exact inheritance pattern from your real application.
 */
@MappedSuperclass
public abstract class BaseAuditEntity {
    
    @Version
    private long version;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;
    
    @UpdateTimestamp
    private LocalDateTime modifiedTime;
    
    // Getters and setters
    public long getVersion() {
        return version;
    }
    
    public void setVersion(long version) {
        this.version = version;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }
    
    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}