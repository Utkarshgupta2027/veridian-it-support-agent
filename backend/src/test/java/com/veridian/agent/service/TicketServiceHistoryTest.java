package com.veridian.agent.service;

import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceHistoryTest {
    @Mock TicketRepository repository;
    private TicketService service;

    @BeforeEach
    void setUp() {
        service = new TicketService(repository);
        when(repository.findTop50ByOrderByCreatedAtDesc()).thenReturn(List.of(
            history("TK-1042", "VPN credential expired", "Resolved (closed)"),
            history("TK-1043", "Laptop replacement (3.2 yrs old)", "Approved - pending fulfillment (active)"),
            history("TK-1044", "Non-catalog software request", "Pending Security review (active)"),
            history("TK-1045", "Mailbox quota increase", "Approved at 35GB (closed)"),
            history("TK-1046", "Printer paper jam, floor 2", "Resolved (closed)"),
            history("TK-1047", "Home office equipment request", "Pending Finance (active)"),
            history("TK-1048", "Phishing email reported", "Escalated to Security - under investigation (active)"),
            history("TK-1049", "Password reset", "Resolved (closed)"),
            history("TK-1050", "Admin access request", "Rejected - no business justification provided (closed)"),
            history("TK-1051", "Guest Wi-Fi issued", "Resolved (closed)")
        ));
    }

    @Test
    void findsAllTenHistoricalTicketsAndPreservesStatus() {
        assertHistory("VPN access is failing", "TK-1042", "closed");
        assertHistory("laptop replacement", "TK-1043", "active");
        assertHistory("non-catalog software", "TK-1044", "active");
        assertHistory("mailbox quota", "TK-1045", "closed");
        assertHistory("printer issue", "TK-1046", "closed");
        assertHistory("home office monitor", "TK-1047", "active");
        assertHistory("phishing email", "TK-1048", "active");
        assertHistory("password reset", "TK-1049", "closed");
        assertHistory("admin access", "TK-1050", "closed");
        assertHistory("guest Wi-Fi", "TK-1051", "closed");
    }

    private void assertHistory(String message, String reference, String status) {
        String result = service.findRelevantHistory(message);
        assertTrue(result.startsWith(reference), message);
        assertTrue(result.toLowerCase().contains(status), message);
    }

    private static Ticket history(String reference, String issue, String status) {
        Ticket ticket = new Ticket(null, status, "HISTORY", "MEDIUM", "IT", status);
        ticket.setReferenceCode(reference);
        ticket.setIssueSummary(issue);
        return ticket;
    }
}
