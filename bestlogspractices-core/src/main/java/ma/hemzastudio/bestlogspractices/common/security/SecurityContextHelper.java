package ma.hemzastudio.bestlogspractices.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
/**
 * Utility to extract the current principal and IP address
 * from Spring Security's SecurityContext and the active
 * HttpServletRequest respectively.
 *-
 * Falls back gracefully for background jobs and tests.
 */
@Component
public class SecurityContextHelper {
    public String currentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "system";
        }
        return auth.getName();
    }
    public String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder
                            .currentRequestAttributes();
            String forwarded = attrs.getRequest()
                    .getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                // X-Forwarded-For may be a comma-separated list
                return forwarded.split(",")[0].trim();
            }
            return attrs.getRequest().getRemoteAddr();
        } catch (IllegalStateException ex) {
            // No active request (scheduled task, test context)
            return "internal";
        }
    }
}