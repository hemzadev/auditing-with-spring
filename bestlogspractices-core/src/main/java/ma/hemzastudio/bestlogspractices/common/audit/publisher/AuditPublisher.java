package ma.hemzastudio.bestlogspractices.common.audit.publisher;

import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import ma.hemzastudio.bestlogspractices.common.audit.event.AuditEvent;
import ma.hemzastudio.bestlogspractices.common.security.SecurityContextHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Facade over {@link ApplicationEventPublisher}.
 *
 * <p>Service classes inject this instead of {@link ApplicationEventPublisher} directly.
 *
 * <p>Actor + IP are resolved automatically from {@link org.springframework.security.core.context.SecurityContext}
 * / request context when available.
 */
@Component
public class AuditPublisher {

  private final ApplicationEventPublisher eventPublisher;
  private final SecurityContextHelper securityHelper;

  public AuditPublisher(ApplicationEventPublisher eventPublisher, SecurityContextHelper securityHelper) {
    this.eventPublisher = eventPublisher;
    this.securityHelper = securityHelper;
  }

  public void publish(String entityType,
                      UUID entityId,
                      AuditAction action,
                      String changeSummary) {
    eventPublisher.publishEvent(new AuditEvent(
        this,
        entityType,
        entityId,
        action,
        securityHelper.currentUser(),
        changeSummary,
        securityHelper.currentIp()
    ));
  }

  /** Overload for mutations with no diff to record (e.g. DELETE). */
  public void publish(String entityType, UUID entityId, AuditAction action) {
    publish(entityType, entityId, action, null);
  }
}
