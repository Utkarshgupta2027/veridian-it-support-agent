package com.veridian.agent.service;

import com.veridian.agent.entity.SupportRequest;
import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.TicketRepository;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    public Ticket create(SupportRequest request, String decision, String priority, String assignedTo, String resolution) {
        String status = decision.equals("RESOLVE") ? "RESOLVED" : (decision.equals("FOLLOW_UP") ? "OPEN" : "PENDING_REVIEW");
        return repository.save(new Ticket(request, status, decision, priority, assignedTo, resolution));
    }

    public String findRelevantHistory(String message) {
        String text = message.toLowerCase();
        String topic = text.contains("vpn") ? "vpn"
            : text.contains("laptop") ? "laptop"
            : text.contains("software") || text.contains("extension") ? "software"
            : text.contains("mailbox") ? "mailbox"
            : text.contains("printer") ? "printer"
            : text.contains("home") || text.contains("monitor") ? "home"
            : text.contains("phish") ? "phish"
            : text.contains("password") || text.contains("locked") ? "password"
            : text.contains("guest") ? "guest"
            : text.contains("admin") ? "admin" : "\u0000";
        return repository.findTop50ByOrderByCreatedAtDesc().stream()
            .filter(ticket -> ((ticket.getIssueSummary() == null ? "" : ticket.getIssueSummary()) + " " + (ticket.getResolution() == null ? "" : ticket.getResolution())).toLowerCase().contains(topic))
            .map(ticket -> (ticket.getReferenceCode() == null ? "Ticket #" + ticket.getId() : ticket.getReferenceCode()) + ": " + ticket.getIssueSummary() + " - " + ticket.getStatus())
            .findFirst()
            .orElse("");
    }
}
