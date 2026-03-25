# bestlogspractices (Spring Boot Starter)

A lightweight Spring Boot Starter that ships **audit trail + soft delete infrastructure** you can reuse across services.

It focuses on:
- **Audit trail** via application events (persisted **after transaction commit**).
- **Soft-delete** via a Hibernate filter (toggleable at service/method level).
- **JPA auditing fields** (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`).

This repository is a **multi-module Maven** project:
- `bestlogspractices-core` → implementation (entities, repositories, aspects, helpers)
- `bestlogspractices-spring-boot-starter` → Spring Boot auto-configuration

---

## Why this exists

In many CRUD apps you end up rewriting the same "boring" infrastructure:
- when/who updated an entity
- soft delete and “show deleted” admin views
- persist audit logs safely, without breaking business transactions

This starter provides those building blocks so your app code stays clean.

---

## Quick start

### 1) Add the dependency

#### Option A — build/install locally (no keys required)

```bash
mvn -DskipTests clean install
```

Then in the consumer project:

```xml
<dependency>
  <groupId>ma.hemzastudio</groupId>
  <artifactId>bestlogspractices-spring-boot-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

#### Option B — Maven Central (planned)

Once released to Maven Central, you’ll be able to use it with the same dependency coordinates (no auth/token required).

---

## Requirements / constraints

- **Java 21+**
- **Spring Boot 3.x**
- Your app must include:
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-aop` (required for the soft-delete aspect)

Optional integrations:
- `spring-boot-starter-security` (for actor resolution via `SecurityContext`)
- `spring-boot-starter-web` (for IP extraction from HTTP request)

Notes:
- If Security is not present or the user is unauthenticated, actor falls back to `system`.
- If the app is non-web, IP falls back to `internal`.

---

## Database

The starter ships an entity `AuditLog` mapped to table `audit_logs`.

If you run with `spring.jpa.hibernate.ddl-auto=update` in dev, Hibernate can create it.

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

---

## Soft delete

To enable soft-delete + auditing fields on your entities, extend:

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
}
```

### Filtering (active vs deleted)

`BaseEntity` defines a Hibernate filter named **`activeOnlyFilter`**.

The starter toggles this filter using annotations:
- `@ActiveOnly` → enables filter (show non-deleted)
- `@IncludeDeleted` → disables filter (admin/audit use cases)

Example:

```java
import ma.hemzastudio.bestlogspractices.common.annotation.ActiveOnly;
import org.springframework.stereotype.Service;

@Service
@ActiveOnly
public class ProductService {
  // repository queries inside will only see active rows
}
```

Admin use:

```java
import ma.hemzastudio.bestlogspractices.common.annotation.IncludeDeleted;
import org.springframework.stereotype.Service;

@Service
@IncludeDeleted
public class ProductAdminService {
  // repository queries can see deleted + active
}
```

---

## Audit trail

Inject `AuditPublisher` and publish an audit event when you mutate an entity.

- Publisher: `ma.hemzastudio.bestlogspractices.common.audit.publisher.AuditPublisher`
- Listener persists the audit row **AFTER_COMMIT** in a **REQUIRES_NEW** transaction.

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

---

## JPA auditing (`createdBy`, `updatedBy`, ...)

The starter enables JPA auditing and provides an `AuditorAware` implementation.

- Uses Spring Security `Authentication#getName()` when available
- Falls back to `system`

---

## Troubleshooting

### Audit logs not being saved

1. Your mutation must run inside a transaction (`@Transactional`).
2. Audit persistence is **AFTER_COMMIT** — if the business transaction rolls back, no audit row is written.
3. If you override async config, ensure a task executor exists.

### Soft-delete filter seems ignored

1. Ensure you have `spring-boot-starter-aop`.
2. Ensure entities extend `BaseEntity`.
3. Apply `@ActiveOnly` / `@IncludeDeleted` on Spring-managed beans (`@Service`).

---

## License

Apache-2.0 — see [LICENSE](./LICENSE).
