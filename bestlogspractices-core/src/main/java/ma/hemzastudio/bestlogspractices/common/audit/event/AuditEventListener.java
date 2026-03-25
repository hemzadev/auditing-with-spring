package ma.hemzastudio.bestlogspractices.common.audit.event;

import ma.hemzastudio.bestlogspractices.common.audit.entity.AuditLog;
import ma.hemzastudio.bestlogspractices.common.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The most critical class in the audit pipeline.
 *
 * <p>Two key decisions:
 * <ol>
 *   <li>{@code @TransactionalEventListener(phase = AFTER_COMMIT)}
 *       → The event is processed only after the originating transaction commits.
 *       If the business transaction rolls back, this method is never called.</li>
 *   <li>{@code @Transactional(propagation = REQUIRES_NEW)}
 *       → Opens a fresh transaction for the audit write so audit failures do not roll back
 *       business data.</li>
 * </ol>
 */
@Component
public class AuditEventListener {

  private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

  private final AuditLogRepository auditLogRepository;

  public AuditEventListener(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

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

      log.info("[AUDIT] {} {} id={} by={}"
          , event.getAction(), event.getEntityType(), event.getEntityId(), event.getActor());

    } catch (Exception ex) {
      // Swallow to avoid impacting business transactions.
      log.error("[AUDIT] Failed to persist audit log for {}/{}: {}"
          , event.getEntityType(), event.getEntityId(), ex.getMessage(), ex);
    }
  }
}
