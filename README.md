# bestlogspractices (Spring Boot Starter)

Reusable Spring Boot starter that provides:

- **Audit trail** via application events (persisted after transaction commit).
- **Soft-delete infrastructure** based on a Hibernate filter that can be enabled/disabled per service/method.
- **JPA auditing fields** (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`) via Spring Data.

This repository is a multi-module Maven project:

- `bestlogspractices-core` → the reusable implementation (entities, repositories, aspects, helpers)
- `bestlogspractices-spring-boot-starter` → Spring Boot auto-configuration that wires the core in any app


## 1) Build and install the starter locally

From the repo root:

```bash
cd C:\projects\personal\practicing\bestlogspractices
mvnw.cmd -DskipTests clean install
```

This installs the artifacts to your local Maven cache (`~/.m2`).


## 2) Add the dependency to your project

In the consumer project `pom.xml`:

```xml
<dependency>
  <groupId>ma.hemzastudio</groupId>
  <artifactId>bestlogspractices-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

That’s it for wiring: the starter auto-configures itself through Spring Boot’s auto-configuration mechanism.


## 3) Ensure your project has the required base dependencies

Your app needs the usual Spring Boot dependencies for JPA + AOP:

- `spring-boot-starter-data-jpa`
- `spring-boot-starter-aop` (required for the soft-delete aspect)

If you want actor/IP resolution from SecurityContext + HTTP request:

- `spring-boot-starter-security` (optional)
- `spring-boot-starter-web` (optional)

Notes:
- `spring-web` and `jakarta.servlet-api` are marked **optional** inside the library, so your app can still be non-web.
- If your app is non-web, IP falls back to `"internal"`.


## 4) Database: create the audit table

The starter ships an entity `AuditLog` mapped to the table `audit_logs`.

If your app uses `spring.jpa.hibernate.ddl-auto=update`, Hibernate can create it automatically.

For production, prefer migrations (Flyway/Liquibase). Minimal PostgreSQL example:

```sql
create table if not exists audit_logs (
  id bigserial primary key,
  entity_type varchar(100) not null,
  entity_id uuid not null,
  action varchar(30) not null,
  actor varchar(150) not null,
  occurred_at timestamp not null,
  change_summary text,
  ip_address varchar(45)
);

create index if not exists idx_audit_entity on audit_logs(entity_type, entity_id);
create index if not exists idx_audit_actor on audit_logs(actor);
create index if not exists idx_audit_timestamp on audit_logs(occurred_at);
create index if not exists idx_audit_action on audit_logs(action);
```


## 5) Soft delete: extend BaseEntity

To enable soft-delete + auditing fields on your own entities, extend:

- `ma.hemzastudio.bestlogspractices.common.persistence.entity.BaseEntity`

Example:

```java
import jakarta.persistence.*;
import ma.hemzastudio.bestlogspractices.common.persistence.entity.BaseEntity;

@Entity
public class Product extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private java.util.UUID id;

  private String name;

  // getters/setters for domain fields only
}
```

### How filtering works

`BaseEntity` defines a Hibernate filter named **`activeOnlyFilter`**.
The library provides an AOP aspect that toggles it using annotations:

- `@ActiveOnly` → enable filter (typically show non-deleted)
- `@IncludeDeleted` → disable filter (admin/audit use-cases)

Annotate a service class or a specific service method.

Example:

```java
import ma.hemzastudio.bestlogspractices.common.annotation.ActiveOnly;
import org.springframework.stereotype.Service;

@Service
@ActiveOnly
public class ProductService {
  // repository queries inside here will only see active rows
}
```

For admin/audit services:

```java
import ma.hemzastudio.bestlogspractices.common.annotation.IncludeDeleted;
import org.springframework.stereotype.Service;

@Service
@IncludeDeleted
public class ProductAdminService {
  // repository queries can see deleted + active rows
}
```


## 6) Audit trail: publish audit events

Inject `AuditPublisher` and publish an audit event whenever you mutate an entity.

- Publisher: `ma.hemzastudio.bestlogspractices.common.audit.publisher.AuditPublisher`
- Listener: `AuditEventListener` persists the row **AFTER_COMMIT** in a **REQUIRES_NEW** transaction.

Example:

```java
import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import ma.hemzastudio.bestlogspractices.common.audit.publisher.AuditPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductWriteService {

  private final AuditPublisher auditPublisher;

  public ProductWriteService(AuditPublisher auditPublisher) {
    this.auditPublisher = auditPublisher;
  }

  public void renameProduct(UUID id, String newName) {
    // ... mutate entity in a @Transactional method

    auditPublisher.publish(
        "Product",
        id,
        AuditAction.UPDATE,
        "{\"name\":{\"from\":\"Old\",\"to\":\"New\"}}"
    );
  }
}
```


## 7) JPA auditing (created/updated by)

The starter enables JPA auditing automatically:

- `@EnableJpaAuditing(auditorAwareRef = "auditorProvider")`

It also provides `AuditorAwareImpl` (bean name `auditorProvider`) which:

- uses Spring Security `Authentication#getName()` when available
- falls back to `"system"` when unauthenticated


## 8) What the starter auto-configures for you

Auto-config class:

- `ma.hemzastudio.bestlogspractices.starter.BestLogsPracticesAutoConfiguration`

It:

- enables async execution (`@EnableAsync`) because audit listener is `@Async`
- enables JPA auditing (`@EnableJpaAuditing`)
- scans library components (`@ComponentScan`)
- registers library JPA repositories (`@EnableJpaRepositories`)
- registers library entities (`@EntityScan`)

So consumer projects do **not** need to share the same base package.


## Troubleshooting

### Audit logs not being saved

Checklist:

1. Your mutation method must run inside a transaction (`@Transactional`).
2. The audit event is persisted **AFTER_COMMIT**, so if the business transaction rolls back, no audit row is written.
3. Ensure async execution is available (starter enables it). If you override async config, ensure task executor is present.

### Soft-delete filter seems ignored

1. Ensure your app includes `spring-boot-starter-aop`.
2. Ensure your entities extend `BaseEntity` (filter is defined there).
3. Apply `@ActiveOnly` / `@IncludeDeleted` on a Spring-managed `@Service` (AOP needs a proxied bean).


## Versioning / publishing

Right now this is `0.1.0-SNAPSHOT` and is installed locally via `mvn install`.
To share across machines/CI, publish to Nexus/Artifactory/GitHub Packages (or release to Maven Central).
