package com.veridian.agent.service;

import com.veridian.agent.dto.AgentRequest;
import com.veridian.agent.dto.AgentResponse;
import com.veridian.agent.entity.KnowledgeBase;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceAssignmentTest {
    @Mock
    private SupportRequestRepository requestRepository;

    @Mock
    private KnowledgeService knowledgeService;

    @Mock
    private TicketService ticketService;

    @Mock
    private AuditService auditService;

    @Mock
    private LlmService llmService;

    @Mock
    private AuditLogRepository auditLogRepository;

    private AgentService agentService;

    @BeforeEach
    void setUp() {
        agentService = new AgentService(
            requestRepository,
            knowledgeService,
            ticketService,
            auditService,
            llmService,
            auditLogRepository
        );
        when(requestRepository.save(any(SupportRequest.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(knowledgeService.search(anyString())).thenReturn(List.of());
        lenient().when(llmService.decide(anyString(), anyString())).thenReturn(Optional.empty());
        when(auditLogRepository.findByRequestIdOrderByTimestampAsc(any())).thenReturn(List.of());
        when(ticketService.findRelevantHistory(anyString())).thenReturn("");
        lenient().when(ticketService.create(any(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(new Ticket());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("assignmentRequests")
    void handlesAllAssignmentRequests(
        String requestId,
        String message,
        String expectedDecision,
        String expectedSource,
        String expectedDepartment
    ) {
        AgentResponse response = handle(message);

        assertEquals(expectedDecision, response.decision(), requestId);
        assertTrue(response.source().contains(expectedSource), requestId + " source");
        assertEquals(expectedDepartment, response.assignedTo(), requestId + " department");
        assertNotNull(response.nextAction(), requestId + " next action");
    }

    @Test
    void guestWifiDoesNotCreateTicket() {
        AgentResponse response = handle(
            "Can I get Wi-Fi access for a guest visiting our office tomorrow?"
        );

        assertEquals("RESOLVE", response.decision());
        assertNull(response.ticketId());
        assertEquals("NO_TICKET_REQUIRED", response.ticketStatus());
        verify(ticketService, never()).create(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void phishingNeverEncouragesForwarding() {
        AgentResponse response = handle(
            "I think I got a phishing email asking for my login - forwarding it to a few teammates to check."
        );

        assertEquals("ESCALATE", response.decision());
        assertEquals("Security", response.assignedTo());
        assertTrue(response.response().contains("security@veridian-corp.example"));
        assertTrue(response.response().toLowerCase().contains("do not forward"));
    }

    @Test
    void ambiguousRequestAsksForInformation() {
        AgentResponse response = handle("Hey can you help, it's not working.");

        assertEquals("FOLLOW_UP", response.decision());
        assertEquals("NONE", response.source());
        assertTrue(
            response.nextAction().toLowerCase().contains("service")
                || response.nextAction().toLowerCase().contains("device")
        );
    }

    @Test
    void historyIsReturnedForRelevantRequest() {
        when(ticketService.findRelevantHistory(anyString()))
            .thenReturn("TK-1042: VPN credential expired - Resolved (closed)");

        AgentResponse response = handle("My VPN credentials expired.");

        assertEquals("TK-1042: VPN credential expired - Resolved (closed)", response.historyContext());
        assertEquals("RESOLVE", response.decision());
    }

    @Test
    void rejectsHallucinatedLlmApprovalAndDepartment() {
        when(knowledgeService.search(anyString())).thenReturn(List.of(
            new KnowledgeBase("KB-01", "Password Reset", "Employees can reset their own password.")
        ));
        when(llmService.decide(anyString(), anyString())).thenReturn(Optional.of(
            new LlmService.Decision(
                "Unknown",
                "RESOLVE",
                "KB-01",
                "Access approved by the Root Admin team.",
                "HIGH",
                "Root Admin"
            )
        ));

        AgentResponse response = handle("Please process this unfamiliar request.");

        assertEquals("FOLLOW_UP", response.decision());
        assertEquals("IT", response.assignedTo());
        assertFalse(response.response().toLowerCase().contains("approved"));
    }

    @Test
    void asksWhetherVpnEmployeeIsFullTimeOrContractor() {
        AgentResponse response = handle("I need VPN access.");

        assertEquals("FOLLOW_UP", response.decision());
        assertEquals("KB-02", response.source());
        assertTrue(response.nextAction().toLowerCase().contains("full-time"));
        assertTrue(response.nextAction().toLowerCase().contains("contractor"));
    }

    @Test
    void asksForSoftwareNameAndCatalogStatus() {
        AgentResponse response = handle("I need software installed.");

        assertEquals("FOLLOW_UP", response.decision());
        assertEquals("KB-04", response.source());
        assertTrue(response.nextAction().toLowerCase().contains("software name"));
        assertTrue(response.nextAction().toLowerCase().contains("catalog"));
    }

    @Test
    void asksForLaptopAgeFailureAndTiming() {
        AgentResponse response = handle("I need a new laptop.");

        assertEquals("FOLLOW_UP", response.decision());
        assertEquals("KB-03 / Asset Management Policy", response.source());
        assertTrue(response.nextAction().toLowerCase().contains("age"));
        assertTrue(response.nextAction().toLowerCase().contains("hardware failure"));
        assertTrue(response.nextAction().toLowerCase().contains("timing"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPolicies")
    void handlesEveryKnowledgeBasePolicy(
        String policy,
        String message,
        String expectedDecision,
        String expectedDepartment
    ) {
        AgentResponse response = handle(message);

        assertEquals(expectedDecision, response.decision(), policy + " decision");
        assertTrue(response.source().contains(policy), policy + " source");
        assertEquals(expectedDepartment, response.assignedTo(), policy + " department");
    }

    private AgentResponse handle(String message) {
        return agentService.handle(new AgentRequest(
            "Test Employee",
            "test.employee@veridian-corp.example",
            message
        ));
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

    private static Stream<Arguments> allPolicies() {
        return Stream.of(
            Arguments.of("KB-01", "I forgot my password and need a password reset.", "RESOLVE", "IT"),
            Arguments.of("KB-02", "I am a full-time employee and need VPN access.", "RESOLVE", "IT"),
            Arguments.of("KB-03", "I need a laptop replacement after 3 years of service.", "ROUTE_TO_OTHER_DEPARTMENT", "IT + Finance & Assets"),
            Arguments.of("KB-04", "Can I self-install standard software from the approved catalog?", "RESOLVE", "IT"),
            Arguments.of("KB-05", "The printer has a paper jam.", "FOLLOW_UP", "IT"),
            Arguments.of("KB-06", "My mailbox is full.", "FOLLOW_UP", "IT + Manager"),
            Arguments.of("KB-07", "I need guest Wi-Fi for tomorrow.", "RESOLVE", "Front Desk"),
            Arguments.of("KB-08", "Please give me access to the expense management tool.", "ROUTE_TO_OTHER_DEPARTMENT", "Finance"),
            Arguments.of("KB-09", "I suspect malware on my laptop.", "ESCALATE", "Security"),
            Arguments.of("KB-10", "I work remotely 4 days a week and need a monitor.", "ROUTE_TO_OTHER_DEPARTMENT", "Manager + Finance")
        );
    }
}
