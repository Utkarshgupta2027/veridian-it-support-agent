package com.veridian.agent.dto;

import java.util.List;

public record AgentResponse(
    Long requestId,
    String category,
    String decision,
    String response,
    String source,
    Long ticketId,
    String ticketStatus,
    String assignedTo,
    String nextAction,
    String historyContext,
    List<AuditItem> audit
) {
    public record AuditItem(String action, String details, String timestamp) {}
}
