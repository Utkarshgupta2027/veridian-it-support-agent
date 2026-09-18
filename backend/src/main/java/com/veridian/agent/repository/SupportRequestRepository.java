package com.veridian.agent.repository;
import java.util.List;
 import java.util.Optional;

 import org.springframework.data.jpa.repository.JpaRepository;

import com.veridian.agent.entity.SupportRequest;
public interface SupportRequestRepository extends JpaRepository<SupportRequest,Long>{List<SupportRequest> findTop50ByOrderByCreatedAtDesc(); Optional<SupportRequest> findByRequestCode(String requestCode);}
