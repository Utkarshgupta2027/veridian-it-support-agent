package com.veridian.agent.repository;

import com.veridian.agent.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    Optional<KnowledgeBase> findByPolicyId(String policyId);
    void deleteByPolicyId(String policyId);
}
