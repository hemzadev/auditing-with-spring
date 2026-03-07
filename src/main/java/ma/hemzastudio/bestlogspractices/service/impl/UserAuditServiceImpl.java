package ma.hemzastudio.bestlogspractices.service.impl;

import lombok.RequiredArgsConstructor;
import ma.hemzastudio.bestlogspractices.common.annotation.IncludeDeleted;
import ma.hemzastudio.bestlogspractices.common.audit.entity.AuditLog;
import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import ma.hemzastudio.bestlogspractices.common.audit.publisher.AuditPublisher;
import ma.hemzastudio.bestlogspractices.common.audit.repository.AuditLogRepository;
import ma.hemzastudio.bestlogspractices.common.security.SecurityContextHelper;
import ma.hemzastudio.bestlogspractices.dao.entity.User;
import ma.hemzastudio.bestlogspractices.dao.repository.UserRepository;
import ma.hemzastudio.bestlogspractices.service.UserAuditService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
/**
 * Admin-only audit service.
 *
 * @IncludeDeleted — disables the Hibernate filter for every
 * method in this class. UserRepository.findById() and findAll()
 * will return deleted users too — no second repository needed.
 */
@Service
@IncludeDeleted
@RequiredArgsConstructor
public class UserAuditServiceImpl implements UserAuditService {
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditPublisher auditPublisher;
    private final SecurityContextHelper security;
    /** Full change history for one user — paginated. */
    @Transactional(readOnly = true)
    public Page<AuditLog> getHistory(UUID userId, Pageable pageable) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                        "User", userId, pageable
                );
    }
    /** All soft-deleted users — visible because @IncludeDeleted disables filter. */
    @Transactional(readOnly = true)
    public List<User> findAllDeleted() {
        return userRepository.findAll()
                .stream()
                .filter(User::isDeleted)
                .toList();
    }
    /** Restore a soft-deleted user. */
    @Transactional
    public User restore(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(id)));
        if (!user.isDeleted())
            throw new IllegalStateException("User is not deleted");
        user.restore();
        User saved = userRepository.save(user);
        auditPublisher.publish("User", id, AuditAction.RESTORE);
        return saved;
    }
}