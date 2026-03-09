package ma.hemzastudio.bestlogspractices.common.persistence.repository;

import ma.hemzastudio.bestlogspractices.common.persistence.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.UUID;
/**
 * Every domain repository extends this.
 * It intentionally stays minimal &mdash; the @Filter on BaseEntity
 * handles visibility; no findByIdIncludingDeleted duplication
 * needed here.
 *-
 * Admin / audit queries go through service classes annotated
 * @IncludeDeleted, which disables the filter transparently.
 * -
 * Usage:
 *   public interface UserRepository extends BaseRepository<User> { ... }
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity>
        extends JpaRepository<T, UUID> {
}