package ma.hemzastudio.bestlogspractices.common.audit.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.hemzastudio.bestlogspractices.common.audit.entity.AuditLog;
import ma.hemzastudio.bestlogspractices.common.audit.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The most critical class in the audit pipeline.
 *
 * Two key decisions:
 *
 * 1) @TransactionalEventListener(phase = AFTER_COMMIT)
 *    The event is processed only after the originating tx has committed.
 *    If the business tx rolls back, this method is NEVER called.
 *
 * 2) @Transactional(propagation = REQUIRES_NEW)
 *    Opens a fresh transaction for the audit write so audit failures do NOT roll back business data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(AuditEvent event) {
        try {
            AuditLog auditLog = AuditLog.of(
                    event.getEntityType(),
                    event.getEntityId(),
                    event.getAction(),
                    event.getActor(),
                    event.getChangeSummary(),
                    event.getIpAddress()
            );
            auditLogRepository.save(auditLog);
            log.info("[AUDIT] {} {} id={} by={}",
                    event.getAction(), event.getEntityType(),
                    event.getEntityId(), event.getActor());
        } catch (Exception ex) {
            // Swallow to avoid impacting business transactions.
            log.error("[AUDIT] Failed to persist audit log for {}/{}: {}",
                    event.getEntityType(), event.getEntityId(), ex.getMessage(), ex);
        }
    }
}
