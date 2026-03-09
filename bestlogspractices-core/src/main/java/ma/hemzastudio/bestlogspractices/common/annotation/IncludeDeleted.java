package ma.hemzastudio.bestlogspractices.common.annotation;


import java.lang.annotation.*;
/**
 * Apply to a @Service class or individual method to DISABLE
 * the "activeOnlyFilter" — exposing soft-deleted records.
 *-
 * IMPORTANT: Only use in admin or audit service classes.
 * Never apply to user-facing services.
 *-
 * Usage:
 *   @Service
 *   @IncludeDeleted      ← audit service sees everything
 *   public class UserAuditService { ... }
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IncludeDeleted {}