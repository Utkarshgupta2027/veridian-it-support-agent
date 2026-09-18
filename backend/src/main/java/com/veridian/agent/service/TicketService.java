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

    public Ticket create(
        SupportRequest request,
        String decision,
        String priority,
        String assignedTo,
        String resolution
    ) {
        String status = statusForDecision(decision);
        return repository.save(new Ticket(request, status, decision, priority, assignedTo, resolution));
    }

    public String findRelevantHistory(String message) {
        String topic = topicFor(message);

        return repository.findTop50ByOrderByCreatedAtDesc().stream()
            .filter(ticket -> searchableTicketText(ticket).contains(topic))
            .map(this::formatHistory)
            .findFirst()
            .orElse("");
    }

    private String statusForDecision(String decision) {
        if (decision.equals("RESOLVE")) {
            return "RESOLVED";
        }
        if (decision.equals("FOLLOW_UP")) {
            return "OPEN";
        }
        return "PENDING_REVIEW";
    }

    private String topicFor(String message) {
        String lowerCaseMessage = message.toLowerCase();
        if (lowerCaseMessage.contains("vpn")) return "vpn";
        if (lowerCaseMessage.contains("laptop")) return "laptop";
        if (lowerCaseMessage.contains("software") || lowerCaseMessage.contains("extension")) return "software";
        if (lowerCaseMessage.contains("mailbox")) return "mailbox";
        if (lowerCaseMessage.contains("printer")) return "printer";
        if (lowerCaseMessage.contains("home") || lowerCaseMessage.contains("monitor")) return "home";
        if (lowerCaseMessage.contains("phish")) return "phish";
        if (lowerCaseMessage.contains("password") || lowerCaseMessage.contains("locked")) return "password";
        if (lowerCaseMessage.contains("guest")) return "guest";
        if (lowerCaseMessage.contains("admin")) return "admin";
        return "\u0000";
    }

    private String searchableTicketText(Ticket ticket) {
        String issue = ticket.getIssueSummary() == null ? "" : ticket.getIssueSummary();
        String resolution = ticket.getResolution() == null ? "" : ticket.getResolution();
        return (issue + " " + resolution).toLowerCase();
    }

    private String formatHistory(Ticket ticket) {
        String reference = ticket.getReferenceCode() == null
            ? "Ticket #" + ticket.getId()
            : ticket.getReferenceCode();
        return reference + ": " + ticket.getIssueSummary() + " - " + ticket.getStatus();
    }
}
