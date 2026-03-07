package ma.hemzastudio.bestlogspractices.config;

import ma.hemzastudio.bestlogspractices.common.security.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
/**
 * @EnableAsync is required because AuditEventListener
 * uses @Async to write audit logs off the main thread.
 *
 * @EnableJpaAuditing must reference the auditorProvider bean
 * by name to populate @CreatedBy / @LastModifiedBy.
 */
@Configuration
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ma.hemzastudio")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {
    @Bean("auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}