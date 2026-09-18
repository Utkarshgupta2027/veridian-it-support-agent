package com.veridian.agent.repository;
import java.util.Optional;

 import org.springframework.data.jpa.repository.JpaRepository;

 import com.veridian.agent.entity.KnowledgeBase;
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase,Long>{Optional<KnowledgeBase> findByPolicyId(String policyId); void deleteByPolicyId(String policyId);}
