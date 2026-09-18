package com.veridian.agent.repository;
import com.veridian.agent.entity.Ticket; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface TicketRepository extends JpaRepository<Ticket,Long>{List<Ticket> findTop50ByOrderByCreatedAtDesc();}
