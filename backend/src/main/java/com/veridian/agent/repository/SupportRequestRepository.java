package com.veridian.agent.repository;

import com.veridian.agent.entity.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    List<SupportRequest> findTop50ByOrderByCreatedAtDesc();
    Optional<SupportRequest> findByRequestCode(String requestCode);
}
