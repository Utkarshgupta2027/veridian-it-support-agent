package com.veridian.agent.service;
import org.springframework.stereotype.Service;

 import com.veridian.agent.entity.SupportRequest;
 import com.veridian.agent.entity.Ticket;
import com.veridian.agent.repository.TicketRepository;
@Service public class TicketService {
 private final TicketRepository repo; public TicketService(TicketRepository r){repo=r;}
 public Ticket create(SupportRequest r,String d,String p,String a,String x){
    String s=d.equals("RESOLVE")?"RESOLVED":(d.equals("FOLLOW_UP")?"OPEN":"PENDING_REVIEW");
  return repo.save(new Ticket(r,s,d,p,a,x));
 }
 public String findRelevantHistory(String message){String text=message.toLowerCase(); String topic=text.contains("vpn")?"vpn":text.contains("laptop")?"laptop":text.contains("software")||text.contains("extension")?"software":text.contains("mailbox")?"mailbox":text.contains("printer")?"printer":text.contains("home")||text.contains("monitor")?"home":text.contains("phish")?"phish":text.contains("password")||text.contains("locked")?"password":text.contains("guest")?"guest":text.contains("admin")?"admin":"\u0000"; return repo.findTop50ByOrderByCreatedAtDesc().stream().filter(ticket->((ticket.getIssueSummary()==null?"":ticket.getIssueSummary())+" "+(ticket.getResolution()==null?"":ticket.getResolution())).toLowerCase().contains(topic)).map(ticket->(ticket.getReferenceCode()==null?"Ticket #"+ticket.getId():ticket.getReferenceCode())+": "+ticket.getIssueSummary()+" - "+ticket.getStatus()).findFirst().orElse("");}
}
