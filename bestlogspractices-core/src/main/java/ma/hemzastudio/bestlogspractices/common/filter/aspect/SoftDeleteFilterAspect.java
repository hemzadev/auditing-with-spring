package ma.hemzastudio.bestlogspractices.common.filter.aspect;

import lombok.RequiredArgsConstructor;
import ma.hemzastudio.bestlogspractices.common.filter.service.SoftDeleteFilterService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
/**
 * Intercepts service methods annotated with @ActiveOnly or
 * @IncludeDeleted and applies the corresponding Hibernate
 * session filter BEFORE the method body executes.
 *-
 * Pointcut strategy:
 *   @within  → class-level annotation (all methods inherit)
 *   @annotation → method-level annotation (single method override)
 *-
 * Execution order:
 *   Request enters service → Aspect fires → filter enabled on Session
 *   → repository query executes with injected WHERE clause
 *   → transaction commits → Session closed → filter discarded
 */
@Aspect
@Component
@RequiredArgsConstructor
public class SoftDeleteFilterAspect {
    private final SoftDeleteFilterService filterService;
    // ── @ActiveOnly — class level ───────────────────────────────────────────────
    @Before("@within(ma.hemzastudio.bestlogspractices.common.annotation.ActiveOnly)")
    public void applyActiveFilterOnClass() {
        filterService.enableActiveOnly();
    }
    // ── @ActiveOnly — method level ──────────────────────────────────────────────
    @Before("@annotation(ma.hemzastudio.bestlogspractices.common.annotation.ActiveOnly)")
    public void applyActiveFilterOnMethod() {
        filterService.enableActiveOnly();
    }
    // ── @IncludeDeleted — class level ───────────────────────────────────────────
    @Before("@within(ma.hemzastudio.bestlogspractices.common.annotation.IncludeDeleted)")
    public void disableFilterOnClass() {
        filterService.disableFilter();
    }
    // ── @IncludeDeleted — method level ──────────────────────────────────────────
    @Before("@annotation(ma.hemzastudio.bestlogspractices.common.annotation.IncludeDeleted)")
    public void disableFilterOnMethod() {
        filterService.disableFilter();
    }
}