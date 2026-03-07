package ma.hemzastudio.bestlogspractices.common.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;
/**
 * Called automatically by Spring Data JPA before every
 * INSERT and UPDATE to populate @CreatedBy / @LastModifiedBy.
 *-
 * Bean name must match auditorAwareRef in @EnableJpaAuditing.
 */
@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of("system");
        }
        return Optional.of(auth.getName());
    }
}