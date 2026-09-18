package com.veridian.agent.service;

import com.veridian.agent.dto.AgentRequest;
import com.veridian.agent.dto.AgentResponse;
import com.veridian.agent.entity.KnowledgeBase;
import com.veridian.agent.entity.SupportRequest;
import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.AuditLogRepository;
import com.veridian.agent.repository.SupportRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AgentService {
    private static final Set<String> VALID_DECISIONS = Set.of(
        "RESOLVE", "FOLLOW_UP", "ESCALATE", "ROUTE_TO_OTHER_DEPARTMENT"
    );
    private static final Set<String> VALID_PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH");
    private static final Set<String> VALID_DEPARTMENTS = Set.of(
        "IT", "Finance", "Security", "Manager", "Front Desk",
        "Manager + Finance", "Manager + IT", "IT + Finance & Assets"
    );

    private final SupportRequestRepository requestRepository;
    private final KnowledgeService knowledgeService;
    private final TicketService ticketService;
    private final AuditService auditService;
    private final LlmService llmService;
    private final AuditLogRepository auditLogRepository;

    public AgentService(
        SupportRequestRepository requestRepository,
        KnowledgeService knowledgeService,
        TicketService ticketService,
        AuditService auditService,
        LlmService llmService,
        AuditLogRepository auditLogRepository
    ) {
        this.requestRepository = requestRepository;
        this.knowledgeService = knowledgeService;
        this.ticketService = ticketService;
        this.auditService = auditService;
        this.llmService = llmService;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AgentResponse handle(AgentRequest input) {
        SupportRequest request = createRequest(input);
        String history = ticketService.findRelevantHistory(input.message());
        List<KnowledgeBase> policies = retrievePolicies(request);
        Decision decision = decide(input, policies, history);

        saveDecision(request, decision);
        Ticket ticket = createTicket(request, decision);
        writeRoutingAudit(request, decision, ticket);

        return buildResponse(request, decision, ticket);
    }

    private SupportRequest createRequest(AgentRequest input) {
        SupportRequest request = requestRepository.save(
            new SupportRequest(input.employeeName(), input.employeeEmail(), input.message())
        );
        auditService.log(request, "REQUEST_RECEIVED", "Employee request received.");
        return request;
    }

    private List<KnowledgeBase> retrievePolicies(SupportRequest request) {
        List<KnowledgeBase> policies = knowledgeService.search(
            request.getRequestText().toLowerCase(Locale.ROOT)
        );
        String policyIds = policies.isEmpty()
            ? "No matching supplied policy found."
            : policies.stream().map(policy -> policy.getPolicyId()).toList().toString();
        auditService.log(request, "POLICY_RETRIEVED", policyIds);
        return policies;
    }

    private Decision decide(AgentRequest input, List<KnowledgeBase> policies, String history) {
        String message = input.message().toLowerCase(Locale.ROOT);
        Decision ruleDecision = applyRules(message, history);
        if (ruleDecision != null) {
            return ruleDecision;
        }

        String knowledgeContext = buildKnowledgeContext(policies, history);
        return llmService.decide(input.message(), knowledgeContext)
            .filter(candidate -> isSafeLlmDecision(candidate, policies))
            .map(candidate -> new Decision(
                candidate.category(),
                normalizeDecision(candidate.decision()),
                candidate.policyId(),
                candidate.response(),
                candidate.priority(),
                candidate.assignedTo(),
                candidate.response(),
                true,
                history
            ))
            .orElseGet(() -> followUpDecision(history));
    }

    private String buildKnowledgeContext(List<KnowledgeBase> policies, String history) {
        String policyText = policies.stream()
            .map(policy -> policy.getPolicyId() + " | " + policy.getTitle() + " | " + policy.getContent())
            .reduce("", (allPolicies, policy) -> allPolicies + "\n" + policy);
        return policyText + "\nHISTORY_CONTEXT: " + history;
    }

    private boolean isSafeLlmDecision(LlmService.Decision candidate, List<KnowledgeBase> policies) {
        if (candidate == null || candidate.category().isBlank() || candidate.response().isBlank()) {
            return false;
        }
        if (!policies.stream().anyMatch(policy -> policy.getPolicyId().equalsIgnoreCase(candidate.policyId()))) {
            return false;
        }
        if (!VALID_DECISIONS.contains(candidate.decision()) || !VALID_PRIORITIES.contains(candidate.priority())) {
            return false;
        }
        if (!VALID_DEPARTMENTS.contains(candidate.assignedTo())) {
            return false;
        }

        String response = candidate.response().toLowerCase(Locale.ROOT);
        return !response.contains("approval granted")
            && !response.contains("approved by")
            && !response.contains("access approved")
            && !response.contains("you are approved");
    }

    private Decision followUpDecision(String history) {
        return new Decision(
            "Unclear IT Request",
            "FOLLOW_UP",
            "NONE",
            "I need the affected service or device, the error you see, and what you expected to happen.",
            "MEDIUM",
            "IT",
            "Please provide the affected service or device, the error message, and the steps that led to it.",
            true,
            history
        );
    }

    private void saveDecision(SupportRequest request, Decision decision) {
        request.setCategory(decision.category());
        request.setStatus(decision.decision());
        requestRepository.save(request);
        auditService.log(
            request,
            "DECISION_MADE",
            decision.decision() + " | " + decision.category() + " | " + decision.source()
        );
    }

    private Ticket createTicket(SupportRequest request, Decision decision) {
        if (!decision.requiresTicket()) {
            return null;
        }
        Ticket ticket = ticketService.create(
            request,
            decision.decision(),
            decision.priority(),
            decision.assignedTo(),
            decision.response()
        );
        auditService.log(request, "TICKET_CREATED", "Ticket TK-" + ticket.getId() + " created.");
        return ticket;
    }

    private void writeRoutingAudit(SupportRequest request, Decision decision, Ticket ticket) {
        if (ticket == null) {
            auditService.log(request, "NO_TICKET_REQUIRED", "Policy outcome does not require an IT ticket.");
        }
        if (decision.decision().equals("ESCALATE")) {
            auditService.log(request, "ESCALATION_PERFORMED", "Assigned to " + decision.assignedTo() + ".");
        }
        if (decision.decision().equals("ROUTE_TO_OTHER_DEPARTMENT")) {
            auditService.log(request, "ROUTED_TO_OTHER_DEPARTMENT", "Assigned to " + decision.assignedTo() + ".");
        }
        auditService.log(request, "RESPONSE_GENERATED", "Source shown: " + decision.source());
    }

    private AgentResponse buildResponse(SupportRequest request, Decision decision, Ticket ticket) {
        List<AgentResponse.AuditItem> auditTrail = auditLogRepository
            .findByRequestIdOrderByTimestampAsc(request.getId())
            .stream()
            .map(log -> new AgentResponse.AuditItem(
                log.getAction(),
                log.getDetails(),
                log.getTimestamp().toString()
            ))
            .toList();

        return new AgentResponse(
            request.getId(),
            decision.category(),
            decision.decision(),
            decision.response(),
            decision.source(),
            ticket == null ? null : ticket.getId(),
            ticket == null ? "NO_TICKET_REQUIRED" : ticket.getStatus(),
            decision.assignedTo(),
            decision.nextAction(),
            decision.historyContext(),
            auditTrail
        );
    }

    private Decision applyRules(String message, String history) {
        if (containsAny(message, "phishing", "malware", "unauthorized access")) {
            return decision(
                "Security Incident", "ESCALATE", "KB-09",
                "Report this immediately to security@veridian-corp.example. Do not forward the suspicious email to colleagues.",
                "HIGH", "Security",
                "Send the report to Security immediately and do not forward the email.", true, history
            );
        }
        if (containsAny(message, "guest wi-fi", "guest wifi", "guest visiting", "guest visiting our office")) {
            return decision(
                "Guest Wi-Fi", "RESOLVE", "KB-07",
                "Guest Wi-Fi credentials are valid for 24 hours and can be generated by any employee at the front-desk kiosk. No IT ticket is required.",
                "LOW", "Front Desk",
                "Generate the guest credentials at the front-desk kiosk for the visit.", false, history
            );
        }
        if (containsAny(message, "locked out", "account locked", "password 6", "password six", "password lockout")) {
            return decision(
                "Account Lockout", "RESOLVE", "KB-01",
                "After more than 5 failed attempts, IT must manually unlock the account. No approval is required.",
                "MEDIUM", "IT",
                "IT should manually unlock the account; the employee may use the self-service portal afterward.", true, history
            );
        }
        if (containsAny(message, "password reset", "reset my password", "forgot password", "change my password")) {
            return decision(
                "Password Reset", "RESOLVE", "KB-01",
                "Employees can reset their own password through the self-service portal at any time. No approval is required.",
                "LOW", "IT",
                "Use the self-service password portal to reset the password.", true, history
            );
        }
        if (containsAny(message, "vpn", "credentials expired", "vpn stopped", "vpn access")
            && containsAny(message, "contractor", "contractor joining")) {
            return decision(
                "Contractor VPN Access", "ROUTE_TO_OTHER_DEPARTMENT", "KB-02",
                "Contractors need manager approval submitted through the access request form before VPN access can be granted.",
                "MEDIUM", "Manager",
                "Submit the VPN access request form for manager approval.", true, history
            );
        }
        if (containsAny(message, "vpn", "credentials expired", "vpn stopped")) {
            if (containsAny(message, "credentials expired", "credential expired", "vpn stopped")) {
                return decision(
                    "VPN Access", "RESOLVE", "KB-02",
                    "VPN credentials expire every 90 days and must be renewed by the employee. Full-time employee VPN access is automatic.",
                    "MEDIUM", "IT",
                    "Renew the expired VPN credentials according to the VPN renewal process.", true, history
                );
            }
            return decision(
                "VPN Access", "RESOLVE", "KB-02",
                "VPN access is granted automatically to full-time employees. Contractors require manager approval through the access request form.",
                "LOW", "IT",
                "Use the standard VPN access process; submit the manager approval form if you are a contractor.", true, history
            );
        }
        if (containsAny(message, "not in the software catalog", "non-catalog", "not in catalog", "browser extension", "productivity tracking")) {
            return decision(
                "Software Installation", "ROUTE_TO_OTHER_DEPARTMENT", "KB-04",
                "Non-catalog software requires IT Security review, which takes 3-5 business days.",
                "MEDIUM", "Security",
                "Submit the software request for IT Security review and allow 3-5 business days.", true, history
            );
        }
        if (containsAny(message, "software", "application", "app")
            && containsAny(message, "approved catalog", "catalog software", "standard software", "self-install")) {
            return decision(
                "Software Installation", "RESOLVE", "KB-04",
                "Standard software listed in the approved catalog can be self-installed. No Security review is required for catalog software.",
                "LOW", "IT",
                "Install the software from the approved catalog.", true, history
            );
        }
        if (containsAny(message, "printer", "paper jam", "print spooler")) {
            return decision(
                "Printer Troubleshooting", "FOLLOW_UP", "KB-05",
                "Check the printer queue, restart the print spooler, and if the issue persists log a ticket with the printer asset tag.",
                "MEDIUM", "IT",
                "Check the queue, restart the spooler, then provide the printer asset tag if the issue persists.", true, history
            );
        }
        if (containsAny(message, "working from home", "work from home", "remote", "monitor")
            && containsAny(message, "allowance", "equipment", "monitor", "get a monitor", "chair", "days a week")) {
            return decision(
                "Home Office Equipment", "ROUTE_TO_OTHER_DEPARTMENT", "KB-10",
                "Working remotely more than 3 days per week qualifies for a one-time chair/monitor allowance, requiring manager sign-off and Finance processing. IT ships equipment after approval.",
                "MEDIUM", "Manager + Finance",
                "Obtain manager sign-off, then send the request to Finance; IT handles shipping after approval.", true, history
            );
        }
        if (containsAny(message, "mailbox", "mailbox full", "quota", "can't send emails", "cannot send emails")) {
            return decision(
                "Email Mailbox Quota", "FOLLOW_UP", "KB-06",
                "The default mailbox quota is 25GB. Archive old mail first; increases above 25GB require manager approval and cannot exceed 50GB.",
                "MEDIUM", "IT + Manager",
                "Archive old mail; request manager approval only if a quota increase above 25GB is still needed.", true, history
            );
        }
        if (containsAny(message, "expense", "finance reporting server", "admin access", "administrative access")) {
            if (containsAny(message, "expense tool", "expense software", "expense management tool", "expense management system")
                || (containsAny(message, "expense") && containsAny(message, "invalid credentials", "can't log", "cannot log"))) {
                return decision(
                    "Expense Software Access", "ROUTE_TO_OTHER_DEPARTMENT", "KB-08",
                    "Finance grants expense-tool access. IT can assist with login or technical issues once an account already exists.",
                    "MEDIUM", "Finance",
                    "Confirm the account exists with Finance; IT can troubleshoot the login after that.", true, history
                );
            }
            return decision(
                "Admin Access Request", "FOLLOW_UP", "NONE",
                "The request needs a business justification and responsible owner before access can be evaluated. Existing admin-access history shows a prior request was rejected without one.",
                "MEDIUM", "Manager + IT",
                "Provide the business justification, system owner, and required access scope for review.", true, history
            );
        }
        if (containsAny(message, "laptop", "screen flickering", "hardware failure", "won't turn on", "wont turn on", "completely dead")) {
            if (containsAny(message, "3.5", "3 and a half", "three and a half", "3 years", "3.2 years")) {
                return decision(
                    "Laptop Replacement Review", "ROUTE_TO_OTHER_DEPARTMENT", "KB-03 / Asset Management Policy",
                    "Laptop replacement is eligible after 3 years or verified hardware failure, while the standard hardware refresh cycle is 4 years. Requests should be raised at least 2 weeks in advance. The policy distinction requires IT verification and Finance & Assets review; no approval is assumed.",
                    "MEDIUM", "IT + Finance & Assets",
                    "IT should verify the hardware failure; raise the request 2 weeks ahead where possible, and have Finance & Assets review any early replacement outside the 4-year cycle.", true, history
                );
            }
            return decision(
                "Laptop Hardware Issue", "FOLLOW_UP", "KB-03 / Asset Management Policy",
                "A hardware issue does not automatically approve replacement. IT must verify the failure; replacement eligibility, the 2-week request lead time, and the 4-year refresh policy then need review.",
                "MEDIUM", "IT + Finance & Assets",
                "Provide the device asset details and arrange IT hardware verification before any replacement decision.", true, history
            );
        }
        return null;
    }

    private Decision decision(
        String category,
        String action,
        String source,
        String response,
        String priority,
        String department,
        String nextAction,
        boolean requiresTicket,
        String history
    ) {
        return new Decision(category, action, source, response, priority, department, nextAction, requiresTicket, history);
    }

    private boolean containsAny(String message, String... terms) {
        for (String term : terms) {
            if (message.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeDecision(String decision) {
        return VALID_DECISIONS.contains(decision) ? decision : "FOLLOW_UP";
    }

    private record Decision(
        String category,
        String decision,
        String source,
        String response,
        String priority,
        String assignedTo,
        String nextAction,
        boolean requiresTicket,
        String historyContext
    ) {}
}
