package com.veridian.agent.repository;
import java.util.List;
 import java.util.Optional;

 import org.springframework.data.jpa.repository.JpaRepository;

import com.veridian.agent.entity.Ticket;
public interface TicketRepository extends JpaRepository<Ticket,Long>{List<Ticket> findTop50ByOrderByCreatedAtDesc(); Optional<Ticket> findByReferenceCode(String referenceCode);}
