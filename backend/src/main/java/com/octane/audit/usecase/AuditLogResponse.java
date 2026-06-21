package com.octane.audit.usecase;

import com.octane.audit.domain.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    String username,
    String action,
    String entityType,
    UUID entityId,
    String details,
    LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
            log.getId(), log.getUsername(), log.getAction().name(),
            log.getEntityType(), log.getEntityId(),
            log.getDetails(), log.getCreatedAt()
        );
    }
}
