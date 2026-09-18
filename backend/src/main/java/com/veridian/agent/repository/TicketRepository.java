package com.veridian.agent.repository;

import com.veridian.agent.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findTop50ByOrderByCreatedAtDesc();
    Optional<Ticket> findByReferenceCode(String referenceCode);
}
