# bestlogspractices Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.hemzadev/bestlogspractices-spring-boot-starter?label=Maven%20Central&color=blue)](https://central.sonatype.com/artifact/io.github.hemzadev/bestlogspractices-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](./LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)

A lightweight Spring Boot Starter that ships **audit trail + soft delete infrastructure** you can reuse across services.

It focuses on:
- **Audit trail** via application events (persisted **after transaction commit**).
- **Soft-delete** via a Hibernate filter (toggleable at service/method level).
- **JPA auditing fields** (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`).

---

## Modules

This project is published as two separate artifacts on Maven Central. Understanding when to use each:

| Module | Purpose |
|--------|---------|
| `bestlogspractices-core` | The implementation — entities, repositories, aspects, and audit helpers. This is where all the functionality lives. |
| `bestlogspractices-spring-boot-starter` | Spring Boot auto-configuration that registers and wires the core beans for you. No manual setup required. |

> **Both are required together.** The core provides the features; the starter makes them work automatically in your Spring Boot app.

---

## Published on Maven Central

All artifacts are available on Maven Central under the group `io.github.hemzadev` at version `0.1.0`.

| Artifact | Browse |
|----------|--------|
| `bestlogspractices-spring-boot-starter` | [Browse on Maven Central](https://central.sonatype.com/artifact/io.github.hemzadev/bestlogspractices-spring-boot-starter/0.1.0) |
| `bestlogspractices-core` | [Browse on Maven Central](https://central.sonatype.com/artifact/io.github.hemzadev/bestlogspractices-core/0.1.0) |
| `bestlogspractices-parent` | [Browse on Maven Central](https://central.sonatype.com/artifact/io.github.hemzadev/bestlogspractices-parent/0.1.0) |

---

## Quick Start

### Add the dependencies

You need **both** artifacts: the core provides all the functionality, and the starter wires everything automatically into your Spring Boot application.

```xml
<!-- Core: entities, repositories, aspects, audit helpers -->
<dependency>
  <groupId>io.github.hemzadev</groupId>
  <artifactId>bestlogspractices-core</artifactId>
  <version>0.1.0</version>
</dependency>

<!-- Starter: Spring Boot auto-configuration for the core -->
<dependency>
  <groupId>io.github.hemzadev</groupId>
  <artifactId>bestlogspractices-spring-boot-starter</artifactId>
  <version>0.1.0</version>
</dependency>
```

---

## Requirements

- **Java 21+**
- **Spring Boot 3.x**

Your app must also include:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

Optional integrations:

- `spring-boot-starter-security` — enables actor resolution via `SecurityContext`. Falls back to `system` if absent or unauthenticated.
- `spring-boot-starter-web` — enables IP extraction from HTTP requests. Falls back to `internal` for non-web apps.

---

## Database

The starter ships an entity `AuditLog` mapped to the table `audit_logs`.

In development, `spring.jpa.hibernate.ddl-auto=update` will create it automatically.

For production, use Flyway or Liquibase. Minimal PostgreSQL migration:

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

create index if not exists idx_audit_entity    on audit_logs(entity_type, entity_id);
create index if not exists idx_audit_actor     on audit_logs(actor);
create index if not exists idx_audit_timestamp on audit_logs(occurred_at);
create index if not exists idx_audit_action    on audit_logs(action);
```

---

## Soft Delete

Extend `BaseEntity` to enable soft-delete and JPA auditing fields on your entities:

```java
import jakarta.persistence.*;
import ma.hemzastudio.bestlogspractices.common.persistence.entity.BaseEntity;

@Entity
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    private String name;
}
```

### Filtering active vs. deleted rows

`BaseEntity` defines a Hibernate filter named `activeOnlyFilter`. Two annotations control it:

| Annotation | Effect |
|-----------|--------|
| `@ActiveOnly` | Enables the filter — queries only return non-deleted rows. |
| `@IncludeDeleted` | Disables the filter — queries return both active and deleted rows. |

Standard service (active rows only):

```java
import ma.hemzastudio.bestlogspractices.common.annotation.ActiveOnly;
import org.springframework.stereotype.Service;

@Service
@ActiveOnly
public class ProductService {
    // repository queries inside will only see active rows
}
```

Admin/audit service:

```java
import ma.hemzastudio.bestlogspractices.common.annotation.IncludeDeleted;
import org.springframework.stereotype.Service;

@Service
@IncludeDeleted
public class ProductAdminService {
    // repository queries can see deleted + active rows
}
```

---

## Audit Trail

Inject `AuditPublisher` and publish an event when you mutate an entity. The listener persists the audit row **after commit** in a separate `REQUIRES_NEW` transaction — so a rollback in your business transaction will never create a spurious audit entry.

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
        // ... mutate entity inside a @Transactional method

        auditPublisher.publish(
            "Product",
            id,
            AuditAction.UPDATE,
            "{\"name\":{\"from\":\"Old\",\"to\":\"New\"}}"
        );
    }
}
```

---

## JPA Auditing (`createdBy`, `updatedBy`, …)

The starter enables JPA auditing and provides an `AuditorAware` implementation automatically.

- Resolves the current actor from Spring Security's `Authentication#getName()` when available.
- Falls back to `system` when Security is absent or the user is unauthenticated.

---

## Troubleshooting

### Audit logs not being saved

1. Your mutation must run inside a `@Transactional` method.
2. Audit persistence is `AFTER_COMMIT` — if the business transaction rolls back, no audit row is written (by design).
3. If you override the async configuration, ensure a task executor bean is present.

### Soft-delete filter seems ignored

1. Confirm `spring-boot-starter-aop` is on the classpath.
2. Confirm entities extend `BaseEntity`.
3. Apply `@ActiveOnly` / `@IncludeDeleted` on Spring-managed beans (`@Service`, `@Component`, etc.), not on plain Java objects.

---

## License

Apache-2.0 — see [LICENSE](./LICENSE).
