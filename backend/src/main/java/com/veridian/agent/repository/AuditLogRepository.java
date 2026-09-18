package com.veridian.agent.repository;
import com.veridian.agent.entity.AuditLog; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface AuditLogRepository extends JpaRepository<AuditLog,Long>{List<AuditLog> findByRequestIdOrderByTimestampAsc(Long requestId);}
