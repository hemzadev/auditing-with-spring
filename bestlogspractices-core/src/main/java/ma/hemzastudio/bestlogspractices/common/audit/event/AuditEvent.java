package ma.hemzastudio.bestlogspractices.common.audit.event;

import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Published to the Spring event bus at the end of any service method that mutates state.
 *
 * <p>The {@link AuditEventListener} picks this up AFTER the originating transaction commits
 * (phase = AFTER_COMMIT), so:
 * <ul>
 *   <li>Only successful operations are recorded</li>
 *   <li>Rolled-back transactions leave zero phantom audit rows</li>
 *   <li>Audit write failure doesn't roll back the business transaction</li>
 * </ul>
 */
public class AuditEvent extends ApplicationEvent {

  private final String entityType;     // e.g. "User", "Product"
  private final UUID entityId;
  private final AuditAction action;
  private final String actor;          // username / "system"
  private final String changeSummary;  // JSON diff or null
  private final String ipAddress;      // from RequestContextHolder

  public AuditEvent(
      Object source,
      String entityType,
      UUID entityId,
      AuditAction action,
      String actor,
      String changeSummary,
      String ipAddress
  ) {
    super(source);
    this.entityType = entityType;
    this.entityId = entityId;
    this.action = action;
    this.actor = actor;
    this.changeSummary = changeSummary;
    this.ipAddress = ipAddress;
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

  public String getChangeSummary() {
    return changeSummary;
  }

  public String getIpAddress() {
    return ipAddress;
  }
}
