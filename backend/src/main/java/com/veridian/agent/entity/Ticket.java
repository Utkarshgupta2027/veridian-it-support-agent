package com.veridian.agent.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="tickets")
public class Ticket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="request_id",nullable=false) private SupportRequest request;
 private String status,decision,priority,assignedTo;
 @Column(columnDefinition="TEXT") private String resolution;
 private LocalDateTime createdAt;
 public Ticket(){}
 public Ticket(SupportRequest r,String s,String d,String p,String a,String x){request=r;status=s;decision=d;priority=p;assignedTo=a;resolution=x;createdAt=LocalDateTime.now();}
 @PrePersist void pre(){if(createdAt==null)createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public SupportRequest getRequest(){return request;} public String getStatus(){return status;} public String getDecision(){return decision;}
 public String getPriority(){return priority;} public String getAssignedTo(){return assignedTo;} public String getResolution(){return resolution;} public LocalDateTime getCreatedAt(){return createdAt;}
}
