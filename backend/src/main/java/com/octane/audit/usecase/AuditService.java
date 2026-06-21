package com.octane.audit.usecase;

import com.octane.audit.domain.AuditAction;
import com.octane.audit.domain.AuditLog;
import com.octane.audit.domain.repository.AuditLogRepository;
import com.octane.user.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, UUID entityId, String details) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String username = "SYSTEM";
            UUID userId = null;

            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                username = auth.getName();
                userId = userRepository.findByUsername(username).map(u -> u.getId()).orElse(null);
            }

            var entry = new AuditLog(userId, username, AuditAction.valueOf(action), entityType, entityId, details);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            logger.warn("Falha ao registrar audit log: {}", e.getMessage());
        }
    }
}
