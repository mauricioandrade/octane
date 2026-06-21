package com.octane.audit.handler;

import com.octane.audit.domain.repository.AuditLogRepository;
import com.octane.audit.usecase.AuditLogResponse;
import com.octane.shared.pagination.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditHandler {

    private final AuditLogRepository auditLogRepository;

    public AuditHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size);

        var springPage = (entityType != null && entityId != null)
            ? auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
            : auditLogRepository.findAll(pageable);

        return PageResponse.of(
            springPage.getContent().stream().map(AuditLogResponse::from).toList(),
            page, size, springPage.getTotalElements()
        );
    }
}
