package com.veridian.agent.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="audit_logs")
public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="request_id",nullable=false) private SupportRequest request;
 private String action;
 @Column(columnDefinition="TEXT") private String details;
 private LocalDateTime timestamp;
 public AuditLog(){}
 public AuditLog(SupportRequest r,String a,String d){request=r;action=a;details=d;timestamp=LocalDateTime.now();}
 @PrePersist void pre(){if(timestamp==null)timestamp=LocalDateTime.now();}
 public Long getId(){return id;} public String getAction(){return action;} public String getDetails(){return details;} public LocalDateTime getTimestamp(){return timestamp;}
}
