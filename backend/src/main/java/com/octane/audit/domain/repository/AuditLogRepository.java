package com.octane.audit.domain.repository;

import com.octane.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditLogRepository {
    AuditLog save(AuditLog log);
    Page<AuditLog> findAll(Pageable pageable);
    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);
}
