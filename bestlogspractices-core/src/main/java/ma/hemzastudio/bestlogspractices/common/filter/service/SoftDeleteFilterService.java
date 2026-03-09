package ma.hemzastudio.bestlogspractices.common.filter.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
/**
 * Manages the Hibernate "activeOnlyFilter" on the current
 * Session. This is the ONLY place the filter is touched —
 * all callers go through SoftDeleteFilterAspect instead.
 */
@Service
public class SoftDeleteFilterService {
    @PersistenceContext
    private EntityManager entityManager;
    /** Enable filter: only rows where deleted = false are visible. */
    public void enableActiveOnly() {
        entityManager.unwrap(Session.class)
                .enableFilter("activeOnlyFilter")
                .setParameter("isDeleted", false);
    }
    /** Enable filter: only rows where deleted = true are visible. */
    public void enableDeletedOnly() {
        entityManager.unwrap(Session.class)
                .enableFilter("activeOnlyFilter")
                .setParameter("isDeleted", true);
    }
    /** Disable filter entirely: all rows visible regardless of deleted flag. */
    public void disableFilter() {
        entityManager.unwrap(Session.class)
                .disableFilter("activeOnlyFilter");
    }
}