package com.veridian.agent.entity;
import java.time.LocalDateTime;

 import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
@Entity @Table(name="tickets")
public class Ticket {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @JsonIgnore @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="request_id",nullable=false) private SupportRequest request;
 private String status,decision,priority,assignedTo,referenceCode,issueSummary;
 @Column(columnDefinition="TEXT") private String resolution;
 private LocalDateTime createdAt;
 public Ticket(){}
 public Ticket(SupportRequest r,String s,String d,String p,String a,String x){request=r;status=s;decision=d;priority=p;assignedTo=a;resolution=x;createdAt=LocalDateTime.now();}
 @PrePersist void pre(){if(createdAt==null)createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public SupportRequest getRequest(){return request;} public String getStatus(){return status;} public String getDecision(){return decision;}
 public String getReferenceCode(){return referenceCode;} public String getIssueSummary(){return issueSummary;}
 public String getPriority(){return priority;} public String getAssignedTo(){return assignedTo;} public String getResolution(){return resolution;} public LocalDateTime getCreatedAt(){return createdAt;}
 public void setReferenceCode(String value){referenceCode=value;} public void setIssueSummary(String value){issueSummary=value;}
}
