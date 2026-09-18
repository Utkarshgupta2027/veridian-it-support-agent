package com.veridian.agent.service;

import com.veridian.agent.dto.AgentRequest;
import com.veridian.agent.dto.AgentResponse;
import com.veridian.agent.entity.SupportRequest;
import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.AuditLogRepository;
import com.veridian.agent.repository.SupportRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentServiceAssignmentTest {
    @Mock SupportRequestRepository requests;
    @Mock KnowledgeService knowledge;
    @Mock TicketService tickets;
    @Mock AuditService audit;
    @Mock LlmService llm;
    @Mock AuditLogRepository audits;
    private AgentService agent;

    @BeforeEach
    void setUp() {
        agent = new AgentService(requests, knowledge, tickets, audit, llm, audits);
        when(requests.save(any(SupportRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(knowledge.search(anyString())).thenReturn(List.of());
        lenient().when(llm.decide(anyString(), anyString())).thenReturn(Optional.empty());
        when(audits.findByRequestIdOrderByTimestampAsc(any())).thenReturn(List.of());
        when(tickets.findRelevantHistory(anyString())).thenReturn("");
        lenient().when(tickets.create(any(), anyString(), anyString(), anyString(), anyString())).thenReturn(new Ticket());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("assignmentRequests")
    void handlesAllAssignmentRequests(String id, String message, String decision, String source, String department) {
        AgentResponse response = agent.handle(new AgentRequest("Test Employee", "test.employee@veridian-corp.example", message));

        assertEquals(decision, response.decision(), id);
        assertTrue(response.source().contains(source), id + " source");
        assertEquals(department, response.assignedTo(), id + " department");
        assertNotNull(response.nextAction(), id + " next action");
    }

    @Test
    void guestWifiDoesNotCreateTicket() {
        AgentResponse response = agent.handle(new AgentRequest("Vikram Chawla", "vikram.chawla@veridian-corp.example", "Can I get Wi-Fi access for a guest visiting our office tomorrow?"));

        assertEquals("RESOLVE", response.decision());
        assertNull(response.ticketId());
        assertEquals("NO_TICKET_REQUIRED", response.ticketStatus());
        verify(tickets, never()).create(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void phishingNeverEncouragesForwarding() {
        AgentResponse response = agent.handle(new AgentRequest("Ananya Reddy", "ananya.reddy@veridian-corp.example", "I think I got a phishing email asking for my login - forwarding it to a few teammates to check."));

        assertEquals("ESCALATE", response.decision());
        assertEquals("Security", response.assignedTo());
        assertTrue(response.response().contains("security@veridian-corp.example"));
        assertTrue(response.response().toLowerCase().contains("do not forward"));
    }

    @Test
    void ambiguousRequestAsksForInformation() {
        AgentResponse response = agent.handle(new AgentRequest("Rahul Menon", "rahul.menon@veridian-corp.example", "Hey can you help, it's not working."));

        assertEquals("FOLLOW_UP", response.decision());
        assertEquals("NONE", response.source());
        assertTrue(response.nextAction().toLowerCase().contains("service") || response.nextAction().toLowerCase().contains("device"));
    }

    @Test
    void historyIsReturnedForRelevantRequest() {
        when(tickets.findRelevantHistory(anyString())).thenReturn("TK-1042: VPN credential expired - Resolved (closed)");

        AgentResponse response = agent.handle(new AgentRequest("Sanjay Oberoi", "sanjay.oberoi@veridian-corp.example", "My VPN credentials expired."));

        assertEquals("TK-1042: VPN credential expired - Resolved (closed)", response.historyContext());
        assertEquals("RESOLVE", response.decision());
    }

    private static Stream<Arguments> assignmentRequests() {
        return Stream.of(
            Arguments.of("REQ-01", "My laptop won't turn on at all, it's completely dead, had it about 3.5 years now.", "ROUTE_TO_OTHER_DEPARTMENT", "KB-03", "IT + Finance & Assets"),
            Arguments.of("REQ-02", "Can I get Wi-Fi access for a guest visiting our office tomorrow?", "RESOLVE", "KB-07", "Front Desk"),
            Arguments.of("REQ-03", "I'm locked out of my account, tried my password 6 times.", "RESOLVE", "KB-01", "IT"),
            Arguments.of("REQ-04", "Need approval to install a data-analysis tool that's not in the software catalog.", "ROUTE_TO_OTHER_DEPARTMENT", "KB-04", "Security"),
            Arguments.of("REQ-05", "My VPN stopped working this morning, says credentials expired.", "RESOLVE", "KB-02", "IT"),
            Arguments.of("REQ-06", "Printer on the 3rd floor keeps showing paper jam even though there's no jam.", "FOLLOW_UP", "KB-05", "IT"),
            Arguments.of("REQ-07", "I've started working from home 4 days a week, how do I get a monitor?", "ROUTE_TO_OTHER_DEPARTMENT", "KB-10", "Manager + Finance"),
            Arguments.of("REQ-08", "I think I got a phishing email asking for my login.", "ESCALATE", "KB-09", "Security"),
            Arguments.of("REQ-09", "My mailbox is full and I can't send emails.", "FOLLOW_UP", "KB-06", "IT + Manager"),
            Arguments.of("REQ-10", "Can someone give me admin access to the finance reporting server?", "FOLLOW_UP", "NONE", "Manager + IT"),
            Arguments.of("REQ-11", "New contractor joining my team next week, they'll need VPN access.", "ROUTE_TO_OTHER_DEPARTMENT", "KB-02", "Manager"),
            Arguments.of("REQ-12", "I can't log into the expense tool, keeps saying invalid credentials.", "ROUTE_TO_OTHER_DEPARTMENT", "KB-08", "Finance"),
            Arguments.of("REQ-13", "Laptop screen is flickering on and off, had it 2 years, might just need a fix not a replacement.", "FOLLOW_UP", "KB-03", "IT + Finance & Assets"),
            Arguments.of("REQ-14", "Requesting approval to install a browser extension for productivity tracking.", "ROUTE_TO_OTHER_DEPARTMENT", "KB-04", "Security"),
            Arguments.of("REQ-15", "Hey can you help, it's not working.", "FOLLOW_UP", "NONE", "IT")
        );
    }
}
