package com.octane.audit.repository;

import com.octane.audit.domain.AuditLog;
import com.octane.audit.domain.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    public AuditLogRepositoryImpl(AuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditLog save(AuditLog log) {
        return jpaRepository.save(log);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return jpaRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable) {
        return jpaRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }
}
