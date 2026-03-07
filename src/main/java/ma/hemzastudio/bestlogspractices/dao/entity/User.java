package ma.hemzastudio.bestlogspractices.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ma.hemzastudio.bestlogspractices.common.persistence.entity.BaseEntity;
import ma.hemzastudio.bestlogspractices.dao.enums.UserStatus;

import java.util.UUID;
/**
 * Example entity demonstrating zero-boilerplate auditing.
 *-
 * No audit annotations here — everything is inherited from
 * BaseEntity. The @Filter is also inherited, so the
 * activeOnlyFilter applies automatically to every JPQL/
 * Criteria query that returns User.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email",    columnList = "email",    unique = true),
                @Index(name = "idx_users_username", columnList = "username", unique = true),
                @Index(name = "idx_users_deleted",  columnList = "deleted")
        }
)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;
    protected User() {}
    public User(String email, String username) {
        this.email = email;
        this.username = username;
    }
}