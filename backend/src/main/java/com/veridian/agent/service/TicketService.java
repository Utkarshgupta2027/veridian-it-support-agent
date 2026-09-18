package com.veridian.agent.service;
import com.veridian.agent.entity.*; import com.veridian.agent.repository.TicketRepository; import org.springframework.stereotype.Service;
@Service public class TicketService {
 private final TicketRepository repo; public TicketService(TicketRepository r){repo=r;}
 public Ticket create(SupportRequest r,String d,String p,String a,String x){
  String s=d.equals("RESOLVE")?"RESOLVED":(d.equals("FOLLOW_UP")?"OPEN":"ESCALATED");
  return repo.save(new Ticket(r,s,d,p,a,x));
 }
}
