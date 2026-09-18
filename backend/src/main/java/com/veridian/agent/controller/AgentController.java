package com.veridian.agent.controller;
import com.veridian.agent.dto.*; import com.veridian.agent.entity.Ticket; import com.veridian.agent.repository.*; import com.veridian.agent.service.AgentService; import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api")
public class AgentController {
 private final AgentService agent; private final SupportRequestRepository requests; private final TicketRepository tickets; private final AuditLogRepository audits;
 public AgentController(AgentService a,SupportRequestRepository r,TicketRepository t,AuditLogRepository al){agent=a;requests=r;tickets=t;audits=al;}
 @PostMapping("/agent/chat") public ResponseEntity<AgentResponse> chat(@Valid @RequestBody AgentRequest x){return ResponseEntity.ok(agent.handle(x));}
 @GetMapping("/requests") public List<?> requests(){return requests.findTop50ByOrderByCreatedAtDesc();}
 @GetMapping("/tickets") public List<Ticket> tickets(){return tickets.findTop50ByOrderByCreatedAtDesc();}
 @GetMapping("/tickets/{id}") public ResponseEntity<Ticket> ticket(@PathVariable Long id){return tickets.findById(id).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());}
 @GetMapping("/audit/{requestId}") public List<?> audit(@PathVariable Long requestId){return audits.findByRequestIdOrderByTimestampAsc(requestId);}
}
