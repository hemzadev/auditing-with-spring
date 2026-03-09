package ma.hemzastudio.bestlogspractices.starter;

import ma.hemzastudio.bestlogspractices.common.audit.entity.AuditLog;
import ma.hemzastudio.bestlogspractices.common.audit.repository.AuditLogRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Auto-configuration for the BestLogsPractices Spring Boot starter.
 *
 * What it does:
 * - Registers the library components (@Component/@Service/@Aspect) via component scan.
 * - Enables async execution (AuditEventListener uses @Async).
 * - Enables Spring Data JPA auditing (@CreatedBy/@LastModifiedBy).
 * - Registers JPA repositories and entities shipped by the library.
 *
 * What it intentionally does NOT do:
 * - Provide demo security/users/controllers. Consumer apps keep full control.
 */
@AutoConfiguration
@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@ComponentScan(basePackages = "ma.hemzastudio.bestlogspractices.common")
@EnableJpaRepositories(basePackageClasses = AuditLogRepository.class)
@EntityScan(basePackageClasses = AuditLog.class)
public class BestLogsPracticesAutoConfiguration {
}
