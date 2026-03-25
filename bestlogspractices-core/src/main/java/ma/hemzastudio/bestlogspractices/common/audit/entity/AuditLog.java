package ma.hemzastudio.bestlogspractices.common.audit.entity;

import jakarta.persistence.*;
import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit log table.
 *
 * <p>Design rules:
 * <ul>
 *   <li>Does NOT extend BaseEntity — audit rows are never soft-deleted</li>
 *   <li>No {@code @Version} — we never update audit rows</li>
 *   <li>No Hibernate filter — audit queries must always see everything</li>
 *   <li>Indexed on (entity_type, entity_id) for fast history lookup</li>
 *   <li>Indexed on actor and occurred_at for compliance reporting</li>
 * </ul>
 *
 * <p>The {@code change_summary} column stores a JSON diff:
 * <pre>
 * {"price":{"from":10.00,"to":15.00},"status":{"from":"ACTIVE","to":"DISCONTINUED"}}
 * </pre>
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_actor", columnList = "actor"),
        @Index(name = "idx_audit_timestamp", columnList = "occurred_at"),
        @Index(name = "idx_audit_action", columnList = "action")
    }
)
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  @Column(name = "entity_id", nullable = false, columnDefinition = "uuid")
  private UUID entityId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AuditAction action;

  @Column(nullable = false, length = 150)
  private String actor;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt = Instant.now();

  @Column(name = "change_summary", columnDefinition = "TEXT")
  private String changeSummary;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  protected AuditLog() {
  }

  // ── Factory ────────────────────────────────────────────────────────────────
  public static AuditLog of(
      String entityType,
      UUID entityId,
      AuditAction action,
      String actor,
      String changeSummary,
      String ipAddress
  ) {
    AuditLog log = new AuditLog();
    log.entityType = entityType;
    log.entityId = entityId;
    log.action = action;
    log.actor = actor;
    log.changeSummary = changeSummary;
    log.ipAddress = ipAddress;
    return log;
  }

  public Long getId() {
    return id;
  }

  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public AuditAction getAction() {
    return action;
  }

  public String getActor() {
    return actor;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public String getChangeSummary() {
    return changeSummary;
  }

  public String getIpAddress() {
    return ipAddress;
  }
}
