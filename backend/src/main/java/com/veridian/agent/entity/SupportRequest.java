package com.veridian.agent.entity;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="requests")
public class SupportRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String employeeName, employeeEmail, category, status;
 @Column(nullable=false,columnDefinition="TEXT") private String requestText;
 private LocalDateTime createdAt;
 public SupportRequest(){}
 public SupportRequest(String n,String e,String m){employeeName=n;employeeEmail=e;requestText=m;createdAt=LocalDateTime.now();}
 @PrePersist void pre(){if(createdAt==null)createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getEmployeeName(){return employeeName;} public String getEmployeeEmail(){return employeeEmail;}
 public String getRequestText(){return requestText;} public String getCategory(){return category;} public String getStatus(){return status;} public LocalDateTime getCreatedAt(){return createdAt;}
 public void setCategory(String v){category=v;} public void setStatus(String v){status=v;}
}
