package ma.hemzastudio.bestlogspractices.common.persistence.entity;

import jakarta.persistence.*;
import jakarta.persistence.Version;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.Instant;
/**
 *  Includes:
 *   • @CreatedDate / @LastModifiedDate   (Spring Data)
 *   • @CreatedBy / @LastModifiedBy       (Spring Data + AuditorAware)
 *   • Soft-delete fields                 (deleted, deletedAt, deletedBy)
 *   • @FilterDef "activeOnlyFilter"      (Hibernate, toggleable via AOP)
 *   • @Version for optimistic locking
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(
        name       = "activeOnlyFilter",
        parameters = @ParamDef(name = "isDeleted", type = Boolean.class),
        defaultCondition = "deleted = :isDeleted"
)
@Filter(name = "activeOnlyFilter")
public abstract class BaseEntity implements Serializable {
    // ── Timestamps ─────────────────────────────────────────────────────────────
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    // ── Actors ─────────────────────────────────────────────────────────────────
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 150)
    private String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by", length = 150)
    private String updatedBy;
    // ── Soft delete ────────────────────────────────────────────────────────────
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
    @Column(name = "deleted_at")
    private Instant deletedAt;
    @Column(name = "deleted_by", length = 150)
    private String deletedBy;
    // ── Optimistic lock ────────────────────────────────────────────────────────
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
    // ── Domain helpers ─────────────────────────────────────────────────────────
    /** Called by service layer — never call directly from controllers. */
    public void markDeleted(String actor) {
        this.deleted   = true;
        this.deletedAt = Instant.now();
        this.deletedBy = actor;
    }
    public void restore() {
        this.deleted   = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
    // ── Getters (no setters — audit fields are system-managed) ─────────────────
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }
    public String  getCreatedBy()  { return createdBy; }
    public String  getUpdatedBy()  { return updatedBy; }
    public boolean isDeleted()     { return deleted; }
    public Instant getDeletedAt()  { return deletedAt; }
    public String  getDeletedBy()  { return deletedBy; }
    public Long    getVersion()    { return version; }
}