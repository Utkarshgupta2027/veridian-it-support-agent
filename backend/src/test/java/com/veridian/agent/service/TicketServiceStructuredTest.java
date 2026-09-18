package com.veridian.agent.service;

import com.veridian.agent.entity.SupportRequest;
import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceStructuredTest {
    @Mock
    private TicketRepository repository;

    @Test
    void persistsTheStructuredTicketContext() {
        SupportRequest request = new SupportRequest(
            "Aditi Sharma",
            "aditi.sharma@veridian-corp.example",
            "My laptop is completely dead."
        );
        when(repository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Ticket ticket = new TicketService(repository).create(
            request,
            "ROUTE_TO_OTHER_DEPARTMENT",
            "MEDIUM",
            "IT + Finance & Assets",
            "Hardware verification is required.",
            "Laptop Replacement Review",
            "KB-03 / Asset Management Policy",
            "Verify hardware failure and review replacement timing."
        );

        assertEquals("Aditi Sharma", ticket.getEmployeeName());
        assertEquals("aditi.sharma@veridian-corp.example", ticket.getEmployeeEmail());
        assertEquals("My laptop is completely dead.", ticket.getIssueSummary());
        assertEquals("Laptop Replacement Review", ticket.getCategory());
        assertEquals("KB-03 / Asset Management Policy", ticket.getKnowledgeSource());
        assertEquals("Verify hardware failure and review replacement timing.", ticket.getNextAction());
        assertEquals("IT + Finance & Assets", ticket.getAssignedTo());
        assertEquals("MEDIUM", ticket.getPriority());
        assertEquals("PENDING_REVIEW", ticket.getStatus());
    }
}
