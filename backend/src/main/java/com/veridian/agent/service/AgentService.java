package com.veridian.agent.service;
import java.util.List;
 import java.util.Locale;
 import java.util.Set;

 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;

 import com.veridian.agent.dto.AgentRequest;
import com.veridian.agent.dto.AgentResponse;
import com.veridian.agent.entity.KnowledgeBase;
import com.veridian.agent.entity.SupportRequest;
import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.AuditLogRepository;
import com.veridian.agent.repository.SupportRequestRepository;
@Service public class AgentService {
 private final SupportRequestRepository requests; private final KnowledgeService kb; private final TicketService tickets; private final AuditService audit; private final LlmService llm; private final AuditLogRepository audits;
 public AgentService(SupportRequestRepository r,KnowledgeService k,TicketService t,AuditService a,LlmService l,AuditLogRepository al){requests=r;kb=k;tickets=t;audit=a;llm=l;audits=al;}
 @Transactional public AgentResponse handle(AgentRequest in){
    SupportRequest request=createEmployeeRequest(in); List<KnowledgeBase> knowledge=retrieveKnowledge(request); Decision decision=matchPolicy(in,knowledge); saveDecision(request,decision); Ticket ticket=createTicket(request,decision); logRouting(request,decision); return buildResponse(request,decision,ticket);
 }
 private SupportRequest createEmployeeRequest(AgentRequest in){SupportRequest request=requests.save(new SupportRequest(in.employeeName(),in.employeeEmail(),in.message())); audit.log(request,"REQUEST_RECEIVED","Employee request received."); return request;}
 private List<KnowledgeBase> retrieveKnowledge(SupportRequest request){List<KnowledgeBase> found=kb.search(request.getRequestText().toLowerCase(Locale.ROOT)); audit.log(request,"POLICY_RETRIEVED",found.isEmpty()?"No matching supplied policy found.":found.stream().map(item->item.getPolicyId()).toList().toString()); return found;}
 private Decision matchPolicy(AgentRequest in,List<KnowledgeBase> found){String message=in.message().toLowerCase(Locale.ROOT); Decision decision=rule(message); if(decision==null){String knowledge=found.stream().map(k->k.getPolicyId()+" | "+k.getTitle()+" | "+k.getContent()).reduce("",(a,b)->a+"\n"+b); var llmDecision=llm.decide(in.message(),knowledge).orElse(null); if(llmDecision!=null&&found.stream().anyMatch(k->k.getPolicyId().equalsIgnoreCase(llmDecision.policyId())))decision=new Decision(llmDecision.category(),normalize(llmDecision.decision()),llmDecision.policyId(),llmDecision.response(),llmDecision.priority(),llmDecision.assignedTo());} return decision==null?new Decision("Unclear IT Request","FOLLOW_UP","NONE","Can you describe what is happening in more detail, including what you expected and what you see now?","MEDIUM","IT"):decision;}
 private void saveDecision(SupportRequest request,Decision decision){request.setCategory(decision.category); request.setStatus(decision.decision); requests.save(request); audit.log(request,"DECISION_MADE",decision.decision+" | "+decision.category+" | "+decision.source);}
 private Ticket createTicket(SupportRequest request,Decision decision){Ticket ticket=tickets.create(request,decision.decision,decision.priority,decision.assignedTo,decision.response); audit.log(request,"TICKET_CREATED","Ticket TK-"+ticket.getId()+" created."); return ticket;}
 private void logRouting(SupportRequest request,Decision decision){if(decision.decision.equals("ESCALATE"))audit.log(request,"ESCALATION_PERFORMED","Assigned to "+decision.assignedTo+"."); if(decision.decision.equals("ROUTE_TO_OTHER_DEPARTMENT"))audit.log(request,"ROUTED_TO_OTHER_DEPARTMENT","Assigned to "+decision.assignedTo+"."); audit.log(request,"RESPONSE_GENERATED","Source shown: "+decision.source);}
 private AgentResponse buildResponse(SupportRequest request,Decision decision,Ticket ticket){List<AgentResponse.AuditItem> trail=audits.findByRequestIdOrderByTimestampAsc(request.getId()).stream().map(x->new AgentResponse.AuditItem(x.getAction(),x.getDetails(),x.getTimestamp().toString())).toList(); return new AgentResponse(request.getId(),decision.category,decision.decision,decision.response,decision.source,ticket.getId(),ticket.getStatus(),decision.assignedTo,trail);}
 private Decision rule(String m){
  if(any(m,"phishing","malware","unauthorized access"))return new Decision("Security Incident","ESCALATE","KB-09","Suspected phishing, malware, or unauthorized access must be reported immediately to Security. The request has been escalated.","HIGH","Security");
  if(any(m,"account locked","locked out","password 6","password six","password lockout"))return new Decision("Account Lockout","RESOLVE","KB-01","Your account appears to be locked. Follow the password/account-unlock procedure in KB-01. A ticket has been recorded.","MEDIUM","IT");
  if(any(m,"vpn credentials expired","vpn credential expired","vpn password expired"))return new Decision("VPN Access","RESOLVE","KB-02","Your VPN credentials have expired. Renew your VPN credentials according to KB-02. A ticket has been recorded.","MEDIUM","IT");
  if(any(m,"expense management","expense software","expense system")&&any(m,"access","permission"))return new Decision("Expense System Access","ROUTE_TO_OTHER_DEPARTMENT","KB-EXPENSE","Access to the expense management system is owned by Finance, not IT. The request has been routed to Finance.","MEDIUM","Finance");
  if(any(m,"laptop is having problems","laptop having problems","laptop problem")&&!any(m,"won't turn on","wont turn on","not turning on"))return new Decision("Laptop Issue","FOLLOW_UP","NONE","Can you describe what is happening? Does the laptop power on?","MEDIUM","IT");
  return null;
 }
 private boolean any(String s,String...x){for(String a:x)if(s.contains(a))return true;return false;}
 private String normalize(String d){return Set.of("RESOLVE","FOLLOW_UP","ESCALATE","ROUTE_TO_OTHER_DEPARTMENT").contains(d)?d:"FOLLOW_UP";}
 private record Decision(String category,String decision,String source,String response,String priority,String assignedTo){}
}
