package com.veridian.agent.service;
import com.veridian.agent.dto.*; import com.veridian.agent.entity.*; import com.veridian.agent.repository.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service public class AgentService {
 private final SupportRequestRepository requests; private final KnowledgeService kb; private final TicketService tickets; private final AuditService audit; private final LlmService llm; private final AuditLogRepository audits;
 public AgentService(SupportRequestRepository r,KnowledgeService k,TicketService t,AuditService a,LlmService l,AuditLogRepository al){requests=r;kb=k;tickets=t;audit=a;llm=l;audits=al;}
 @Transactional public AgentResponse handle(AgentRequest in){
  SupportRequest r=requests.save(new SupportRequest(in.employeeName(),in.employeeEmail(),in.message())); audit.log(r,"REQUEST_RECEIVED","Employee request received.");
  String m=in.message().toLowerCase(Locale.ROOT); List<KnowledgeBase> found=kb.search(m);
  audit.log(r,"POLICY_RETRIEVED",found.isEmpty()?"No matching supplied policy found.":found.stream().map(KnowledgeBase::getPolicyId).toList().toString());
  Decision d=rule(m);
  if(d==null){
   String knowledge=found.stream().map(k->k.getPolicyId()+" | "+k.getTitle()+" | "+k.getContent()).reduce("",(a,b)->a+"\\n"+b);
   var ld=llm.decide(in.message(),knowledge).orElse(null);
   if(ld!=null && found.stream().anyMatch(k->k.getPolicyId().equalsIgnoreCase(ld.policyId()))) d=new Decision(ld.category(),normalize(ld.decision()),ld.policyId(),ld.response(),ld.priority(),ld.assignedTo());
  }
  if(d==null)d=new Decision("Unclear IT Request","FOLLOW_UP","NONE","Can you describe what is happening in more detail, including what you expected and what you see now?","MEDIUM","IT");
  r.setCategory(d.category); r.setStatus(d.decision); requests.save(r); audit.log(r,"DECISION_MADE",d.decision+" | "+d.category+" | "+d.source);
  Ticket t=tickets.create(r,d.decision,d.priority,d.assignedTo,d.response); audit.log(r,"TICKET_CREATED","Ticket TK-"+t.getId()+" created.");
  if(d.decision.equals("ESCALATE"))audit.log(r,"ESCALATION_PERFORMED","Assigned to "+d.assignedTo+".");
  if(d.decision.equals("ROUTE_TO_OTHER_DEPARTMENT"))audit.log(r,"ROUTED_TO_OTHER_DEPARTMENT","Assigned to "+d.assignedTo+".");
  audit.log(r,"RESPONSE_GENERATED","Source shown: "+d.source);
  List<AgentResponse.AuditItem> trail=audits.findByRequestIdOrderByTimestampAsc(r.getId()).stream().map(x->new AgentResponse.AuditItem(x.getAction(),x.getDetails(),x.getTimestamp().toString())).toList();
  return new AgentResponse(r.getId(),d.category,d.decision,d.response,d.source,t.getId(),t.getStatus(),d.assignedTo,trail);
 }
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
