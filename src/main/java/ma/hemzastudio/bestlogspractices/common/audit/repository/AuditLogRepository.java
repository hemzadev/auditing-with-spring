package ma.hemzastudio.bestlogspractices.common.audit.repository;

import ma.hemzastudio.bestlogspractices.common.audit.entity.AuditLog;
import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
/**
 * Note: This repository intentionally does NOT extend BaseRepository
 * because AuditLog is not a BaseEntity — it has no filter, no soft-delete.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /** Full history for a single entity instance (e.g. all changes to User #xyz). */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType, UUID entityId, Pageable pageable
    );
    /** All actions performed by a specific actor. */
    Page<AuditLog> findByActorOrderByOccurredAtDesc(String actor, Pageable pageable);
    /** All actions of a given type within a time window — useful for compliance reports. */
    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.action = :action
          AND a.occurredAt BETWEEN :from AND :to
        ORDER BY a.occurredAt DESC
    """)
    List<AuditLog> findByActionBetween(
            @Param("action") AuditAction action,
            @Param("from")   Instant from,
            @Param("to")     Instant to
    );
    /** All DELETE actions — useful for soft-delete recovery UI. */
    List<AuditLog> findByEntityTypeAndActionOrderByOccurredAtDesc(
            String entityType, AuditAction action
    );
}