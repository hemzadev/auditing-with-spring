package ma.hemzastudio.bestlogspractices.service;

import ma.hemzastudio.bestlogspractices.common.audit.entity.AuditLog;
import ma.hemzastudio.bestlogspractices.dao.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserAuditService {

    public Page<AuditLog> getHistory(UUID userId, Pageable pageable);
    /** All soft-deleted users — visible because @IncludeDeleted disables filter. */
    public List<User> findAllDeleted();
    /** Restore a soft-deleted user. */
    public User restore(UUID id);

}
