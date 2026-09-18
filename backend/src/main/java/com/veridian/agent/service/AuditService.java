package com.veridian.agent.service;
import com.veridian.agent.entity.*; import com.veridian.agent.repository.AuditLogRepository; import org.springframework.stereotype.Service;
@Service public class AuditService {
 private final AuditLogRepository repo; public AuditService(AuditLogRepository r){repo=r;}
 public AuditLog log(SupportRequest r,String a,String d){return repo.save(new AuditLog(r,a,d));}
}
