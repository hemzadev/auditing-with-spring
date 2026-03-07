package ma.hemzastudio.bestlogspractices.dao.repository;

import ma.hemzastudio.bestlogspractices.common.persistence.repository.BaseRepository;
import ma.hemzastudio.bestlogspractices.dao.entity.User;

import java.util.Optional;
import java.util.UUID;
/**
 * Single repository for User — no UserAuditRepository needed.
 *-
 * The activeOnlyFilter is toggled at the service layer via
 * @ActiveOnly / @IncludeDeleted annotations. This repository
 * stays completely unaware of soft-delete mechanics.
 *-
 * Every method here works correctly regardless of whether
 * the filter is active — the aspect handles it before the
 * query executes.
 */
public interface UserRepository extends BaseRepository<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}