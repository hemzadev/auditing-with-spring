package ma.hemzastudio.bestlogspractices.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.hemzastudio.bestlogspractices.common.annotation.ActiveOnly;
import ma.hemzastudio.bestlogspractices.common.audit.enums.AuditAction;
import ma.hemzastudio.bestlogspractices.common.audit.publisher.AuditPublisher;
import ma.hemzastudio.bestlogspractices.common.security.SecurityContextHelper;
import ma.hemzastudio.bestlogspractices.dao.entity.User;
import ma.hemzastudio.bestlogspractices.dao.repository.UserRepository;
import ma.hemzastudio.bestlogspractices.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
/**
 * Standard user service.
 *
 * @ActiveOnly — the SoftDeleteFilterAspect fires before every
 * method in this class and enables the Hibernate filter, so
 * deleted users are NEVER visible here.
 */
@Service
@ActiveOnly
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AuditPublisher auditPublisher;
    private final SecurityContextHelper security;
    @Transactional
    public User create(String email, String username) {
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException(email);
        User saved = userRepository.save(new User(email, username));
        auditPublisher.publish(
                "User", saved.getId(), AuditAction.CREATE,
                String.format(
                        "{\"email\":\"%s\",\"username\":\"%s\"}",
                        email, username
                )
        );
        return saved;
    }
    @Transactional
    public User updateEmail(UUID id, String newEmail) {
        User user    = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(id)));
        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        User saved = userRepository.save(user);
        auditPublisher.publish(
                "User", id, AuditAction.UPDATE,
                String.format(
                        "{\"email\":{\"from\":\"%s\",\"to\":\"%s\"}}",
                        oldEmail, newEmail
                )
        );
        return saved;
    }
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(id)));
        user.markDeleted(security.currentUser());
        userRepository.save(user);
        // No changeSummary needed for deletes
        auditPublisher.publish("User", id, AuditAction.DELETE);
    }
}
