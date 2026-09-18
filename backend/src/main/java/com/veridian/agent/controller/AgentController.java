package com.veridian.agent.controller;

import com.veridian.agent.dto.AgentRequest;
import com.veridian.agent.dto.AgentResponse;
import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.AuditLogRepository;
import com.veridian.agent.repository.SupportRequestRepository;
import com.veridian.agent.repository.TicketRepository;
import com.veridian.agent.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AgentController {
    private final AgentService agentService;
    private final SupportRequestRepository requestRepository;
    private final TicketRepository ticketRepository;
    private final AuditLogRepository auditLogRepository;

    public AgentController(
        AgentService agentService,
        SupportRequestRepository requestRepository,
        TicketRepository ticketRepository,
        AuditLogRepository auditLogRepository
    ) {
        this.agentService = agentService;
        this.requestRepository = requestRepository;
        this.ticketRepository = ticketRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping("/agent/chat")
    public ResponseEntity<AgentResponse> chat(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.ok(agentService.handle(request));
    }

    @GetMapping("/requests")
    public List<?> requests() {
        return requestRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @GetMapping("/tickets")
    public List<Ticket> tickets() {
        return ticketRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> ticket(@PathVariable Long id) {
        return ticketRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/audit/{requestId}")
    public List<?> audit(@PathVariable Long requestId) {
        return auditLogRepository.findByRequestIdOrderByTimestampAsc(requestId);
    }
}
