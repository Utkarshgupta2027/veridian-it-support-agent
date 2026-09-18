package com.veridian.agent.dto;
import java.util.*;
public record AgentResponse(Long requestId,String category,String decision,String response,String source,Long ticketId,String ticketStatus,String assignedTo,List<AuditItem> audit){
 public record AuditItem(String action,String details,String timestamp){}
}
