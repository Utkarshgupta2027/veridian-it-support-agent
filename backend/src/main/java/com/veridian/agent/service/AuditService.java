package com.veridian.agent.service;

import com.veridian.agent.entity.AuditLog;
import com.veridian.agent.entity.SupportRequest;
import com.veridian.agent.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public AuditLog log(SupportRequest request, String action, String details) {
        return repository.save(new AuditLog(request, action, details));
    }
}
