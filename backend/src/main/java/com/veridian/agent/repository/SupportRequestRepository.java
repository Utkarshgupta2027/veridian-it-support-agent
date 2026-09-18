package com.veridian.agent.repository;
import com.veridian.agent.entity.SupportRequest; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SupportRequestRepository extends JpaRepository<SupportRequest,Long>{List<SupportRequest> findTop50ByOrderByCreatedAtDesc();}
