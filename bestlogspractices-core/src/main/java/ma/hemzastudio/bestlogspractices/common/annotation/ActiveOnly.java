package ma.hemzastudio.bestlogspractices.common.annotation;

import java.lang.annotation.*;
/**
 * Apply to a @Service class or individual method to enable
 * the Hibernate "activeOnlyFilter" for that execution scope.
 *-
 * The SoftDeleteFilterAspect intercepts these call sites
 * and enables the filter on the current Hibernate Session.
 -
 * Usage:
 *   @Service
 *   @ActiveOnly          ← entire service sees only active records
 *   public class UserService { ... }
 *
 *   @ActiveOnly          ← single method override
 *   public User findActive(UUID id) { ... }
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActiveOnly {}