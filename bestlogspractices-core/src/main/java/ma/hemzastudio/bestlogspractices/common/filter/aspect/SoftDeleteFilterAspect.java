package ma.hemzastudio.bestlogspractices.common.filter.aspect;

import ma.hemzastudio.bestlogspractices.common.filter.service.SoftDeleteFilterService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * Intercepts service methods annotated with {@code @ActiveOnly} or {@code @IncludeDeleted}
 * and applies the corresponding Hibernate session filter BEFORE the method body executes.
 *
 * <p>Pointcut strategy:
 * <ul>
 *   <li>{@code @within} → class-level annotation (all methods inherit)</li>
 *   <li>{@code @annotation} → method-level annotation (single method override)</li>
 * </ul>
 */
@Aspect
@Component
public class SoftDeleteFilterAspect {

  private final SoftDeleteFilterService filterService;

  public SoftDeleteFilterAspect(SoftDeleteFilterService filterService) {
    this.filterService = filterService;
  }

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
