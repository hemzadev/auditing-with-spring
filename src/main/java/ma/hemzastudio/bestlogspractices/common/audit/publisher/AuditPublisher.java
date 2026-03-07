package ma.hemzastudio.bestlogspractices.common.audit.publisher;

import lombok.RequiredArgsConstructor;
import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import ma.hemzastudio.bestlogspractices.common.audit.event.AuditEvent;
import ma.hemzastudio.bestlogspractices.common.security.SecurityContextHelper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.UUID;
/**
 * Facade over ApplicationEventPublisher.
 *-
 * Service classes inject this instead of ApplicationEventPublisher
 * directly, keeping audit publishing to a single readable line:
 *-
 *   auditPublisher.publish("User", user.getId(), CREATE, diff);
 *-
 * The actor and IP are resolved automatically from SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class AuditPublisher {
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityContextHelper securityHelper;
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